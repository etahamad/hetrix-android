package io.github.etahamad.hetrix.data

import io.github.etahamad.hetrix.data.api.HetrixApiService
import io.github.etahamad.hetrix.data.api.NetworkException
import io.github.etahamad.hetrix.data.local.TokenStorage
import io.github.etahamad.hetrix.data.model.AgentMetricsPointDto
import io.github.etahamad.hetrix.data.model.BlacklistMonitorsResponseDto
import io.github.etahamad.hetrix.data.model.MonitorDto
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.PingResponseDto
import io.github.etahamad.hetrix.data.model.ServerAgentMetricsResponseDto
import io.github.etahamad.hetrix.data.model.UptimeMonitorsResponseDto
import io.github.etahamad.hetrix.data.repository.MonitorRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class MonitorRepositoryTest {

    private lateinit var fakeTokenStorage: FakeTokenStorage
    private lateinit var fakeApiService: FakeHetrixApiService
    private lateinit var repository: MonitorRepositoryImpl

    @Before
    fun setUp() {
        fakeTokenStorage = FakeTokenStorage()
        fakeApiService = FakeHetrixApiService()
        repository = MonitorRepositoryImpl(fakeApiService, fakeTokenStorage)
    }

    @Test
    fun getMonitors_emitsMissingTokenException_whenTokenStorageIsEmpty() = runTest {
        fakeTokenStorage.clearToken()

        val result = repository.getMonitors().first()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.MissingTokenException)
    }

    @Test
    fun getMonitors_returnsMappedServerMonitors_whenSuccessful() = runTest {
        fakeTokenStorage.saveToken("valid_test_token")
        fakeApiService.monitorsResponse = Response.success(
            UptimeMonitorsResponseDto(
                monitors = listOf(
                    MonitorDto(
                        id = "srv-101",
                        name = "Production API Gateway",
                        type = "website",
                        target = "https://api.example.com/health",
                        uptimeStatus = "up",
                        uptimeRatio = 0.9999,
                        responseTimeMs = 38,
                        lastCheckTimestamp = 1700000000L,
                        hasAgent = true
                    ),
                    MonitorDto(
                        id = "srv-102",
                        name = "Primary Database Node",
                        type = "server",
                        target = "10.0.1.5",
                        uptimeStatus = "down",
                        uptimeRatio = 0.9845,
                        responseTimeMs = null,
                        lastCheckTimestamp = 1700000000L,
                        hasAgent = false
                    )
                )
            )
        )

        val result = repository.getMonitors(fetchLiveMetrics = false).first()

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(2, list.size)

        val first = list[0]
        assertEquals("srv-101", first.id)
        assertEquals("Production API Gateway", first.name)
        assertEquals(MonitorStatus.ONLINE, first.status)
        assertEquals(99.99, first.uptimePercentage, 0.001)
        assertEquals(38L, first.responseTimeMs)
        assertTrue(first.hasAgent)

        val second = list[1]
        assertEquals("srv-102", second.id)
        assertEquals(MonitorStatus.OFFLINE, second.status)
        assertFalse(second.hasAgent)
    }

    @Test
    fun validateToken_returnsSuccess_whenApiResponds200() = runTest {
        fakeApiService.pingResponse = Response.success(PingResponseDto(status = "ok", message = "pong"))

        val result = repository.validateToken("valid_token_123")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun validateToken_returnsUnauthorizedException_whenApiResponds401() = runTest {
        val errorJson = """{"status":"ERROR","error":"Invalid API Key provided"}"""
        val errorBody = errorJson.toResponseBody("application/json".toMediaType())
        fakeApiService.pingResponse = Response.error(401, errorBody)

        val result = repository.validateToken("bad_token")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is NetworkException.UnauthorizedException)
        assertEquals("Invalid API Key provided", exception?.message)
    }

    @Test
    fun getMetricsForMonitor_returnsDomainMetrics_whenSuccessful() = runTest {
        fakeApiService.metricsResponse = Response.success(
            ServerAgentMetricsResponseDto(
                stats = listOf(
                    AgentMetricsPointDto(
                        cpuUsage = 42.5,
                        ramUsage = 68.2,
                        diskUsage = 55.0,
                        swapUsage = 12.0,
                        load1 = 0.45,
                        load5 = 0.38,
                        load15 = 0.22,
                        timestamp = 1700000000L
                    )
                )
            )
        )

        val result = repository.getMetricsForMonitor("srv-101")

        assertTrue(result.isSuccess)
        val metrics = result.getOrThrow()
        assertEquals(42.5f, metrics.cpuPercent, 0.01f)
        assertEquals(68.2f, metrics.ramPercent, 0.01f)
        assertEquals(55.0f, metrics.diskPercent, 0.01f)
        assertEquals(12.0f, metrics.swapPercent ?: 0f, 0.01f)
        assertEquals("0.45 0.38 0.22", metrics.loadAverage)
    }

    // --- Test Doubles ---

    private class FakeTokenStorage : TokenStorage {
        private val _flow = MutableStateFlow<String?>(null)
        override val tokenFlow: StateFlow<String?> = _flow

        override fun getToken(): String? = _flow.value
        override fun hasToken(): Boolean = !_flow.value.isNullOrBlank()
        override suspend fun saveToken(token: String) { _flow.value = token }
        override suspend fun clearToken() { _flow.value = null }
    }

    private class FakeHetrixApiService : HetrixApiService {
        var pingResponse: Response<PingResponseDto> = Response.success(PingResponseDto(status = "ok", message = "pong"))
        var monitorsResponse: Response<UptimeMonitorsResponseDto> = Response.success(UptimeMonitorsResponseDto())
        var metricsResponse: Response<ServerAgentMetricsResponseDto> = Response.success(ServerAgentMetricsResponseDto())
        var blacklistResponse: Response<BlacklistMonitorsResponseDto> = Response.success(BlacklistMonitorsResponseDto())

        override suspend fun ping(authOverride: String?): Response<PingResponseDto> {
            return pingResponse
        }

        override suspend fun getUptimeMonitors(authOverride: String?): Response<UptimeMonitorsResponseDto> {
            return monitorsResponse
        }

        override suspend fun getServerAgentMetrics(monitorId: String, authOverride: String?): Response<ServerAgentMetricsResponseDto> {
            return metricsResponse
        }

        override suspend fun getBlacklistMonitors(authOverride: String?): Response<BlacklistMonitorsResponseDto> {
            return blacklistResponse
        }
    }
}
