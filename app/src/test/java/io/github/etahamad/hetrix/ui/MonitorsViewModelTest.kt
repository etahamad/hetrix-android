package io.github.etahamad.hetrix.ui

import io.github.etahamad.hetrix.data.api.NetworkException
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMetrics
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.data.repository.MonitorRepository
import io.github.etahamad.hetrix.ui.main.FilterStatus
import io.github.etahamad.hetrix.ui.main.MonitorsUiState
import io.github.etahamad.hetrix.ui.main.MonitorsViewModel
import io.github.etahamad.hetrix.ui.main.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonitorsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMonitorRepository
    private lateinit var viewModel: MonitorsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMonitorRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isNoToken_whenRepositoryHasNoToken() = runTest {
        fakeRepository.setToken(null)
        viewModel = MonitorsViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(MonitorsUiState.NoToken, state)
        job.cancel()
    }

    @Test
    fun loadMonitors_transitionsToSuccess_whenTokenIsPresent() = runTest {
        fakeRepository.setToken("valid_token")
        fakeRepository.monitors = listOf(
            ServerMonitor(
                id = "1",
                name = "EU Node 1",
                type = "Server",
                target = "eu1.example.com",
                status = MonitorStatus.ONLINE,
                uptimePercentage = 99.9,
                responseTimeMs = 25,
                lastCheckTimestamp = 1700000000L,
                hasAgent = true,
                metrics = ServerMetrics(cpuPercent = 30f, ramPercent = 40f, swapPercent = null, diskPercent = 50f, loadAverage = null, timestamp = 1700000000L)
            )
        )

        viewModel = MonitorsViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MonitorsUiState.Success)
        val success = state as MonitorsUiState.Success
        assertEquals(1, success.monitors.size)
        assertEquals("EU Node 1", success.monitors[0].name)
        job.cancel()
    }

    @Test
    fun searchQuery_filtersMonitorsCorrectly() = runTest {
        fakeRepository.setToken("valid_token")
        fakeRepository.monitors = listOf(
            ServerMonitor(
                id = "1",
                name = "Database Primary",
                type = "Database",
                target = "db.internal",
                status = MonitorStatus.ONLINE,
                uptimePercentage = 99.9,
                responseTimeMs = 15,
                lastCheckTimestamp = 1700000000L,
                hasAgent = false
            ),
            ServerMonitor(
                id = "2",
                name = "Web Front",
                type = "Website",
                target = "https://example.com",
                status = MonitorStatus.ONLINE,
                uptimePercentage = 99.8,
                responseTimeMs = 45,
                lastCheckTimestamp = 1700000000L,
                hasAgent = false
            )
        )

        viewModel = MonitorsViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.updateSearchQuery("database")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MonitorsUiState.Success
        assertEquals(1, state.filteredMonitors.size)
        assertEquals("Database Primary", state.filteredMonitors[0].name)
        job.cancel()
    }

    @Test
    fun sortOption_ordersOfflineServersFirst() = runTest {
        fakeRepository.setToken("valid_token")
        fakeRepository.monitors = listOf(
            ServerMonitor(
                id = "1",
                name = "Alpha Online",
                type = "Server",
                target = "alpha.com",
                status = MonitorStatus.ONLINE,
                uptimePercentage = 99.9,
                responseTimeMs = 20,
                lastCheckTimestamp = 1700000000L,
                hasAgent = false
            ),
            ServerMonitor(
                id = "2",
                name = "Beta Offline",
                type = "Server",
                target = "beta.com",
                status = MonitorStatus.OFFLINE,
                uptimePercentage = 80.0,
                responseTimeMs = null,
                lastCheckTimestamp = 1700000000L,
                hasAgent = false
            )
        )

        viewModel = MonitorsViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.updateSortOption(SortOption.STATUS)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MonitorsUiState.Success
        assertEquals("Beta Offline", state.filteredMonitors[0].name)
        assertEquals("Alpha Online", state.filteredMonitors[1].name)
        job.cancel()
    }

    @Test
    fun saveAndValidateToken_savesWhenValid() = runTest {
        fakeRepository.setToken(null)
        viewModel = MonitorsViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.saveAndValidateToken("new_valid_token")
        advanceUntilIdle()

        assertEquals("new_valid_token", fakeRepository.tokenFlow.value)
        assertFalse(viewModel.isSettingsOpen.value)
    }

    // --- Fake Repository ---

    private class FakeMonitorRepository : MonitorRepository {
        private val _tokenFlow = MutableStateFlow<String?>(null)
        override val tokenFlow: StateFlow<String?> = _tokenFlow

        var monitors: List<ServerMonitor> = emptyList()
        var shouldFailValidation: Boolean = false

        fun setToken(token: String?) {
            _tokenFlow.value = token
        }

        override fun hasToken(): Boolean = !_tokenFlow.value.isNullOrBlank()

        override suspend fun saveToken(token: String) {
            _tokenFlow.value = token
        }

        override suspend fun clearToken() {
            _tokenFlow.value = null
        }

        override suspend fun validateToken(token: String): Result<Boolean> {
            return if (shouldFailValidation || token == "invalid") {
                Result.failure(NetworkException.UnauthorizedException("Invalid Token"))
            } else {
                Result.success(true)
            }
        }

        override fun getMonitors(fetchLiveMetrics: Boolean): Flow<Result<List<ServerMonitor>>> {
            return flowOf(Result.success(monitors))
        }

        override suspend fun getMetricsForMonitor(monitorId: String): Result<ServerMetrics> {
            return Result.success(ServerMetrics(10f, 20f, null, 30f, null, 1700000000L))
        }
    }
}
