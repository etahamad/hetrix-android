package io.github.etahamad.hetrix.ui.main

import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMonitor

/**
 * Sealed hierarchy representing all UI screen states for the Monitors dashboard.
 */
sealed interface MonitorsUiState {
    data object Initial : MonitorsUiState

    data object NoToken : MonitorsUiState

    data class Loading(
        val message: String = "Fetching servers & telemetry…"
    ) : MonitorsUiState

    data class Success(
        val monitors: List<ServerMonitor>,
        val filteredMonitors: List<ServerMonitor>,
        val isRefreshing: Boolean = false,
        val lastRefreshedTimestamp: Long = System.currentTimeMillis()
    ) : MonitorsUiState

    data class Error(
        val message: String,
        val isTokenError: Boolean = false,
        val isRefreshing: Boolean = false
    ) : MonitorsUiState
}

/**
 * Sorting options for server monitor cards.
 */
enum class SortOption(val displayName: String) {
    STATUS("Status (Offline first)"),
    NAME("Server Name (A-Z)"),
    CPU_USAGE("CPU Usage (Highest)"),
    RAM_USAGE("RAM Usage (Highest)"),
    LATENCY("Latency / Ping (Lowest)"),
    UPTIME("Uptime (Lowest)")
}

/**
 * Filtering options by monitor status.
 */
enum class FilterStatus(val displayName: String) {
    ALL("All"),
    ONLINE_ONLY("Online"),
    OFFLINE_ONLY("Offline / Issues"),
    AGENT_SERVERS("With Agent")
}
