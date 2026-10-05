package io.github.etahamad.hetrix.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.etahamad.hetrix.data.api.NetworkException
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.data.repository.MonitorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating HetriX monitor dashboard state, user interactions, and token configuration.
 */
class MonitorsViewModel(
    private val repository: MonitorRepository
) : ViewModel() {

    private val _rawMonitors = MutableStateFlow<List<ServerMonitor>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.STATUS)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)
    val filterStatus: StateFlow<FilterStatus> = _filterStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isValidatingToken = MutableStateFlow(false)
    val isValidatingToken: StateFlow<Boolean> = _isValidatingToken.asStateFlow()

    private val _tokenValidationError = MutableStateFlow<String?>(null)
    val tokenValidationError: StateFlow<String?> = _tokenValidationError.asStateFlow()

    private val _errorState = MutableStateFlow<MonitorsUiState.Error?>(null)
    private val _isLoading = MutableStateFlow(false)

    val currentToken: StateFlow<String?> = repository.tokenFlow

    private var loadJob: Job? = null

    private data class FilterParams(
        val query: String,
        val sort: SortOption,
        val filter: FilterStatus
    )

    private data class SyncState(
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val error: MonitorsUiState.Error?
    )

    private val filterParamsFlow = combine(_searchQuery, _sortOption, _filterStatus) { query, sort, filter ->
        FilterParams(query, sort, filter)
    }

    private val syncStateFlow = combine(_isLoading, _isRefreshing, _errorState) { loading, refreshing, error ->
        SyncState(loading, refreshing, error)
    }

    val uiState: StateFlow<MonitorsUiState> = combine(
        repository.tokenFlow,
        _rawMonitors,
        filterParamsFlow,
        syncStateFlow
    ) { token, monitors, filterParams, syncState ->
        when {
            token.isNullOrBlank() -> MonitorsUiState.NoToken
            syncState.error != null -> syncState.error.copy(isRefreshing = syncState.isRefreshing)
            syncState.isLoading && monitors.isEmpty() -> MonitorsUiState.Loading()
            else -> {
                val filtered = filterAndSortMonitors(
                    monitors = monitors,
                    query = filterParams.query,
                    sort = filterParams.sort,
                    filter = filterParams.filter
                )
                MonitorsUiState.Success(
                    monitors = monitors,
                    filteredMonitors = filtered,
                    isRefreshing = syncState.isRefreshing,
                    lastRefreshedTimestamp = System.currentTimeMillis()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonitorsUiState.Initial
    )

    init {
        // Automatically load monitors when token changes or is first initialized
        viewModelScope.launch {
            repository.tokenFlow.collect { token ->
                if (!token.isNullOrBlank()) {
                    loadMonitors(isPullToRefresh = false)
                } else {
                    _rawMonitors.value = emptyList()
                    _errorState.value = null
                }
            }
        }
    }

    fun loadMonitors(isPullToRefresh: Boolean = false) {
        if (!repository.hasToken()) {
            return
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_rawMonitors.value.isEmpty()) {
                _isLoading.value = true
            }
            _errorState.value = null

            repository.getMonitors(fetchLiveMetrics = true).collect { result ->
                _isLoading.value = false
                _isRefreshing.value = false

                result.onSuccess { monitors ->
                    _rawMonitors.value = monitors
                    _errorState.value = null
                }.onFailure { error ->
                    val isAuthError = error is NetworkException.UnauthorizedException ||
                            error is NetworkException.ForbiddenException ||
                            error is NetworkException.MissingTokenException

                    val message = when (error) {
                        is NetworkException -> error.message
                        else -> error.localizedMessage ?: "Failed to synchronize monitors."
                    }

                    _errorState.value = MonitorsUiState.Error(
                        message = message,
                        isTokenError = isAuthError,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun updateFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
    }

    fun openSettings() {
        _tokenValidationError.value = null
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _tokenValidationError.value = null
        _isSettingsOpen.value = false
    }

    fun saveAndValidateToken(token: String) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            _tokenValidationError.value = "API Bearer token cannot be empty."
            return
        }

        viewModelScope.launch {
            _isValidatingToken.value = true
            _tokenValidationError.value = null

            val validationResult = repository.validateToken(cleanToken)
            _isValidatingToken.value = false

            validationResult.onSuccess {
                repository.saveToken(cleanToken)
                _isSettingsOpen.value = false
                _tokenValidationError.value = null
            }.onFailure { error ->
                _tokenValidationError.value = when (error) {
                    is NetworkException.UnauthorizedException -> "Invalid API Token. HetrixTools rejected this token."
                    is NetworkException.ForbiddenException -> "Access denied. Check your HetrixTools plan permissions."
                    is NetworkException.NoConnectivityException -> "Network offline. Unable to reach HetrixTools API."
                    is NetworkException.TimeoutException -> "Connection timed out during validation."
                    else -> error.localizedMessage ?: "Token validation failed. Please check credentials."
                }
            }
        }
    }

    fun clearToken() {
        viewModelScope.launch {
            repository.clearToken()
            _rawMonitors.value = emptyList()
            _errorState.value = null
        }
    }

    private fun filterAndSortMonitors(
        monitors: List<ServerMonitor>,
        query: String,
        sort: SortOption,
        filter: FilterStatus
    ): List<ServerMonitor> {
        val filtered = monitors.filter { monitor ->
            val matchesQuery = query.isBlank() ||
                    monitor.name.contains(query, ignoreCase = true) ||
                    monitor.target.contains(query, ignoreCase = true) ||
                    monitor.type.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.ONLINE_ONLY -> monitor.status == MonitorStatus.ONLINE
                FilterStatus.OFFLINE_ONLY -> monitor.status != MonitorStatus.ONLINE
                FilterStatus.AGENT_SERVERS -> monitor.hasAgent
            }

            matchesQuery && matchesFilter
        }

        return when (sort) {
            SortOption.STATUS -> filtered.sortedWith(
                compareBy<ServerMonitor> {
                    // Offline and warning items first
                    when (it.status) {
                        MonitorStatus.OFFLINE -> 0
                        MonitorStatus.WARNING -> 1
                        MonitorStatus.UNKNOWN -> 2
                        MonitorStatus.PAUSED -> 3
                        MonitorStatus.ONLINE -> 4
                    }
                }.thenBy { it.name.lowercase() }
            )
            SortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
            SortOption.CPU_USAGE -> filtered.sortedByDescending { it.metrics?.cpuPercent ?: -1f }
            SortOption.RAM_USAGE -> filtered.sortedByDescending { it.metrics?.ramPercent ?: -1f }
            SortOption.LATENCY -> filtered.sortedBy { it.responseTimeMs ?: Long.MAX_VALUE }
            SortOption.UPTIME -> filtered.sortedBy { it.uptimePercentage }
        }
    }
}
