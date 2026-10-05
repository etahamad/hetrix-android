package io.github.etahamad.hetrix.data.repository

import io.github.etahamad.hetrix.data.api.HetrixApiService
import io.github.etahamad.hetrix.data.api.NetworkException
import io.github.etahamad.hetrix.data.api.NetworkUtils
import io.github.etahamad.hetrix.data.local.TokenStorage
import io.github.etahamad.hetrix.data.model.MonitorDto
import io.github.etahamad.hetrix.data.model.ServerMetrics
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.data.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository interface for managing HetrixTools monitors and API token state.
 */
interface MonitorRepository {
    val tokenFlow: StateFlow<String?>

    fun hasToken(): Boolean

    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun validateToken(token: String): Result<Boolean>

    fun getMonitors(fetchLiveMetrics: Boolean = true): Flow<Result<List<ServerMonitor>>>

    suspend fun getMetricsForMonitor(monitorId: String): Result<ServerMetrics>
}

/**
 * Production implementation of [MonitorRepository] coordinating API calls and secure storage.
 */
class MonitorRepositoryImpl(
    private val apiService: HetrixApiService,
    private val tokenStorage: TokenStorage
) : MonitorRepository {

    override val tokenFlow: StateFlow<String?> = tokenStorage.tokenFlow

    override fun hasToken(): Boolean = tokenStorage.hasToken()

    override suspend fun saveToken(token: String) = tokenStorage.saveToken(token)

    override suspend fun clearToken() = tokenStorage.clearToken()

    override suspend fun validateToken(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return@withContext Result.failure(
                NetworkException.ApiException(400, "API Token cannot be blank.")
            )
        }

        try {
            val bearerHeader = "Bearer $cleanToken"
            // Call API with the token override to test validity
            val response = apiService.getMonitors(authOverride = bearerHeader)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                // Also check if monitors are wrapped in envelope
                val envelopeResponse = apiService.getMonitorsEnvelope(authOverride = bearerHeader)
                if (envelopeResponse.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(NetworkUtils.parseHttpError(response))
                }
            }
        } catch (e: Exception) {
            val mappedException = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Validation failed", e)
            Result.failure(mappedException)
        }
    }

    override fun getMonitors(fetchLiveMetrics: Boolean): Flow<Result<List<ServerMonitor>>> = flow {
        if (!hasToken()) {
            emit(Result.failure(NetworkException.MissingTokenException()))
            return@flow
        }

        val result = fetchMonitorsInternal(fetchLiveMetrics)
        emit(result)
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchMonitorsInternal(fetchLiveMetrics: Boolean): Result<List<ServerMonitor>> = withContext(Dispatchers.IO) {
        try {
            val rawMonitors: List<MonitorDto> = try {
                val directResponse = apiService.getMonitors()
                if (directResponse.isSuccessful) {
                    directResponse.body().orEmpty()
                } else {
                    val envelopeResponse = apiService.getMonitorsEnvelope()
                    if (envelopeResponse.isSuccessful) {
                        val body = envelopeResponse.body()
                        body?.monitors?.ifEmpty { body.data }.orEmpty()
                    } else {
                        throw NetworkUtils.parseHttpError(directResponse)
                    }
                }
            } catch (e: Exception) {
                // If direct deserialization failed because response is an envelope
                val envelopeResponse = apiService.getMonitorsEnvelope()
                if (envelopeResponse.isSuccessful) {
                    val body = envelopeResponse.body()
                    body?.monitors?.ifEmpty { body.data }.orEmpty()
                } else {
                    throw if (e is NetworkException) e else NetworkUtils.parseHttpError(envelopeResponse)
                }
            }

            // Convert to domain models
            val initialMonitors = rawMonitors.map { it.toDomain() }

            if (!fetchLiveMetrics) {
                return@withContext Result.success(initialMonitors)
            }

            // Concurrently fetch latest server agent metrics for servers that have agent enabled
            val enrichedMonitors = coroutineScope {
                initialMonitors.map { monitor ->
                    async {
                        if (monitor.hasAgent && monitor.metrics == null) {
                            try {
                                val metricsResult = fetchMetricsDirect(monitor.id)
                                monitor.copy(metrics = metricsResult)
                            } catch (_: Exception) {
                                monitor
                            }
                        } else {
                            monitor
                        }
                    }
                }.awaitAll()
            }

            Result.success(enrichedMonitors)
        } catch (e: Exception) {
            val mapped = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Failed to fetch monitors", e)
            Result.failure(mapped)
        }
    }

    override suspend fun getMetricsForMonitor(monitorId: String): Result<ServerMetrics> = withContext(Dispatchers.IO) {
        try {
            val metrics = fetchMetricsDirect(monitorId)
            if (metrics != null) {
                Result.success(metrics)
            } else {
                Result.failure(NetworkException.ApiException(404, "No metrics available for monitor $monitorId"))
            }
        } catch (e: Exception) {
            val mapped = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Failed to fetch metrics", e)
            Result.failure(mapped)
        }
    }

    private suspend fun fetchMetricsDirect(monitorId: String): ServerMetrics? {
        val response = apiService.getServerAgentMetrics(monitorId)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
        }
        val envelopeResponse = apiService.getServerAgentMetricsEnvelope(monitorId)
        if (envelopeResponse.isSuccessful) {
            val body = envelopeResponse.body()
            return (body?.metrics ?: body?.data)?.toDomain()
        }
        return null
    }
}
