package io.github.etahamad.hetrix.ui.main

import io.github.etahamad.hetrix.data.model.BlacklistMonitor
import io.github.etahamad.hetrix.data.model.ServerMonitor

/**
 * Sealed class modeling UI states for the entire monitoring console.
 */
sealed interface MonitorsUiState {
    data object Initial : MonitorsUiState
    data object NoToken : MonitorsUiState
    data class Loading(
        val message: String = "Fetching infrastructure data & telemetry…"
    ) : MonitorsUiState

    data class Success(
        val monitors: List<ServerMonitor>,
        val filteredMonitors: List<ServerMonitor>,
        val blacklistMonitors: List<BlacklistMonitor> = emptyList(),
        val isRefreshing: Boolean = false,
        val lastRefreshedTimestamp: Long = System.currentTimeMillis()
    ) : MonitorsUiState

    data class Error(
        val message: String,
        val isTokenError: Boolean = false,
        val isRefreshing: Boolean = false,
        val cachedMonitors: List<ServerMonitor>? = null
    ) : MonitorsUiState
}

/**
 * Sorting options for server and uptime monitor cards.
 */
enum class SortOption(val displayName: String) {
    STATUS("Priority / Status"),
    NAME("Name (A-Z)"),
    UPTIME("Uptime %"),
    LATENCY("Response Time"),
    CPU_USAGE("CPU Usage (Highest)"),
    RAM_USAGE("RAM Usage (Highest)");

    val label: String get() = displayName
}

/**
 * Filtering options for uptime / home monitors.
 */
enum class FilterStatus(val displayName: String) {
    ALL("All"),
    ONLINE_ONLY("Operational"),
    OFFLINE_ONLY("Down / Degraded"),
    AGENT_SERVERS("With Agent");

    val label: String get() = displayName
}

/**
 * Filter options for Reputation / Blacklist monitors.
 */
enum class ReputationFilter(val displayName: String) {
    ALL("All"),
    LISTED_ONLY("Listed"),
    CLEAN_ONLY("Clean"),
    UNKNOWN_ONLY("Unknown");

    val label: String get() = displayName
}

/**
 * Auto-refresh interval options.
 */
enum class AutoRefreshInterval(val label: String, val intervalSeconds: Long) {
    OFF("Manual Only", 0L),
    EVERY_30S("Every 30 seconds", 30L),
    EVERY_1M("Every 1 minute", 60L),
    EVERY_5M("Every 5 minutes", 300L)
}
