package io.github.etahamad.hetrix.data.repository

import io.github.etahamad.hetrix.data.api.HetrixApiService
import io.github.etahamad.hetrix.data.api.NetworkException
import io.github.etahamad.hetrix.data.api.NetworkUtils
import io.github.etahamad.hetrix.data.local.TokenStorage
import io.github.etahamad.hetrix.data.model.BlacklistMonitor
import io.github.etahamad.hetrix.data.model.ServerMetrics
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.data.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Repository interface for managing HetrixTools monitors, blacklist checks, and API vault.
 */
interface MonitorRepository {
    val tokenFlow: StateFlow<String?>
    val lastSyncTimestamp: StateFlow<Long?>

    fun hasToken(): Boolean

    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun validateToken(token: String): Result<Boolean>

    suspend fun testConnection(): Result<Long>

    fun getMonitors(fetchLiveMetrics: Boolean = true): Flow<Result<List<ServerMonitor>>>

    fun getBlacklistMonitors(): Flow<Result<List<BlacklistMonitor>>>

    suspend fun getMetricsForMonitor(monitorId: String): Result<ServerMetrics>

    fun clearCachedData()
}

/**
 * Production implementation of [MonitorRepository] coordinating local cache, secure storage, and v3 API.
 */
class MonitorRepositoryImpl(
    private val apiService: HetrixApiService,
    private val tokenStorage: TokenStorage
) : MonitorRepository {

    override val tokenFlow: StateFlow<String?> = tokenStorage.tokenFlow

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    override val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    // In-memory cache for offline resilience and fast switching
    private var cachedMonitors: List<ServerMonitor>? = null
    private var cachedBlacklist: List<BlacklistMonitor>? = null

    override fun hasToken(): Boolean = tokenStorage.hasToken()

    override suspend fun saveToken(token: String) {
        tokenStorage.saveToken(token)
    }

    override suspend fun clearToken() {
        tokenStorage.clearToken()
        clearCachedData()
    }

    override fun clearCachedData() {
        cachedMonitors = null
        cachedBlacklist = null
        _lastSyncTimestamp.value = null
    }

    override suspend fun validateToken(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return@withContext Result.failure(
                NetworkException.ApiException(400, "API Token cannot be blank.")
            )
        }

        try {
            val bearerHeader = "Bearer $cleanToken"
            val response = apiService.ping(authOverride = bearerHeader)
            if (response.isSuccessful && response.body()?.status == "ok") {
                Result.success(true)
            } else {
                Result.failure(NetworkUtils.parseHttpError(response))
            }
        } catch (e: Exception) {
            val mappedException = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Validation failed", e)
            Result.failure(mappedException)
        }
    }

    override suspend fun testConnection(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            var latency = 0L
            val elapsed = measureTimeMillis {
                val response = apiService.ping()
                if (!response.isSuccessful) {
                    throw NetworkUtils.parseHttpError(response)
                }
            }
            latency = elapsed
            Result.success(latency)
        } catch (e: Exception) {
            val mapped = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Connection test failed", e)
            Result.failure(mapped)
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
            val response = apiService.getUptimeMonitors()
            if (!response.isSuccessful) {
                // If we have cached monitors and network fails, preserve cache
                if (cachedMonitors != null) {
                    return@withContext Result.success(cachedMonitors!!)
                }
                throw NetworkUtils.parseHttpError(response)
            }

            val body = response.body()
            val rawMonitors = body?.monitors?.ifEmpty { body.data }.orEmpty()

            // Convert to domain models
            val initialMonitors = rawMonitors.map { it.toDomain() }

            val enrichedMonitors = if (fetchLiveMetrics) {
                coroutineScope {
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
            } else {
                initialMonitors
            }

            cachedMonitors = enrichedMonitors
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Result.success(enrichedMonitors)
        } catch (e: Exception) {
            if (cachedMonitors != null) {
                Result.success(cachedMonitors!!)
            } else {
                val mapped = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Failed to fetch monitors", e)
                Result.failure(mapped)
            }
        }
    }

    override fun getBlacklistMonitors(): Flow<Result<List<BlacklistMonitor>>> = flow {
        if (!hasToken()) {
            emit(Result.failure(NetworkException.MissingTokenException()))
            return@flow
        }

        val result = fetchBlacklistInternal()
        emit(result)
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchBlacklistInternal(): Result<List<BlacklistMonitor>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBlacklistMonitors()
            if (!response.isSuccessful) {
                if (cachedBlacklist != null) {
                    return@withContext Result.success(cachedBlacklist!!)
                }
                throw NetworkUtils.parseHttpError(response)
            }

            val body = response.body()
            val rawList = body?.monitors?.ifEmpty { body.data }.orEmpty()
            val domainList = rawList.map { it.toDomain() }

            cachedBlacklist = domainList
            Result.success(domainList)
        } catch (e: Exception) {
            if (cachedBlacklist != null) {
                Result.success(cachedBlacklist!!)
            } else {
                val mapped = if (e is NetworkException) e else NetworkException.ApiException(-1, e.localizedMessage ?: "Failed to fetch blacklist monitors", e)
                Result.failure(mapped)
            }
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
            val body = response.body()
            return body?.toDomain()
        }
        return null
    }
}
