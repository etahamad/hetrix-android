package io.github.etahamad.hetrix.data.model

/**
 * Domain model representing an uptime monitor or monitored server.
 */
data class ServerMonitor(
    val id: String,
    val name: String,
    val type: String,
    val target: String,
    val status: MonitorStatus,
    val uptimePercentage: Double,
    val responseTimeMs: Long?,
    val lastCheckTimestamp: Long,
    val hasAgent: Boolean,
    val metrics: ServerMetrics? = null
)

/**
 * Domain model representing real-time system performance metrics.
 */
data class ServerMetrics(
    val cpuPercent: Float,
    val ramPercent: Float,
    val swapPercent: Float?,
    val diskPercent: Float,
    val loadAverage: String?,
    val timestamp: Long
)

/**
 * Operational status of a monitored target.
 */
enum class MonitorStatus(val displayName: String) {
    ONLINE("Online"),
    OFFLINE("Offline"),
    WARNING("Warning"),
    PAUSED("Paused"),
    UNKNOWN("Unknown");

    val isOperational: Boolean
        get() = this == ONLINE

    companion object {
        fun fromString(rawStatus: String?): MonitorStatus {
            return when (rawStatus?.trim()?.lowercase()) {
                "online", "up", "ok", "active", "1" -> ONLINE
                "offline", "down", "error", "0" -> OFFLINE
                "warning", "degraded", "issues", "warn" -> WARNING
                "paused", "maintenance", "disabled" -> PAUSED
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Maps [MonitorDto] to clean immutable [ServerMonitor] domain model.
 */
fun MonitorDto.toDomain(overrideMetrics: ServerMetrics? = null): ServerMonitor {
    val effectiveId = id ?: sid ?: "unknown_${hashCode()}"
    val effectiveName = name?.takeIf { it.isNotBlank() }
        ?: label?.takeIf { it.isNotBlank() }
        ?: target?.takeIf { it.isNotBlank() }
        ?: "Monitor $effectiveId"

    val effectiveTarget = target?.takeIf { it.isNotBlank() }
        ?: resolveAddress?.takeIf { it.isNotBlank() }
        ?: url?.takeIf { it.isNotBlank() }
        ?: ipOrHost?.takeIf { it.isNotBlank() }
        ?: "N/A"

    val effectiveType = type?.replaceFirstChar { it.uppercase() } ?: "Website"
    
    // Status resolution prioritizing uptime_status ('up', 'down') and monitor_status ('active', 'paused')
    val rawStatus = when {
        monitorStatus?.equals("paused", ignoreCase = true) == true -> "paused"
        uptimeStatus != null -> uptimeStatus
        status != null -> status
        else -> "unknown"
    }
    val parsedStatus = MonitorStatus.fromString(rawStatus)
    val parsedUptime = parsedUptime ?: (uptimeRatio?.let { it * 100.0 } ?: 100.0)
    val parsedLastCheck = lastCheckTimestamp ?: lastStatusChangeTimestamp ?: createdAt ?: System.currentTimeMillis()
    val agentPresent = hasAgent == true || agentInstalled == true || !agentId.isNullOrBlank()

    return ServerMonitor(
        id = effectiveId,
        name = effectiveName,
        type = effectiveType,
        target = effectiveTarget,
        status = parsedStatus,
        uptimePercentage = parsedUptime.coerceIn(0.0, 100.0),
        responseTimeMs = averageLocationPing,
        lastCheckTimestamp = parsedLastCheck,
        hasAgent = agentPresent,
        metrics = overrideMetrics
    )
}

/**
 * Maps [AgentMetricsPointDto] to [ServerMetrics] domain model.
 */
fun AgentMetricsPointDto.toDomain(): ServerMetrics {
    val cpu = (cpuUsage ?: 0.0).toFloat().coerceIn(0f, 100f)
    val ram = (ramUsage ?: 0.0).toFloat().coerceIn(0f, 100f)
    val swap = swapUsage?.toFloat()?.coerceIn(0f, 100f)
    val disk = (diskUsage ?: 0.0).toFloat().coerceIn(0f, 100f)
    val load = if (load1 != null) {
        String.format(java.util.Locale.US, "%.2f %.2f %.2f", load1, load5 ?: load1, load15 ?: load1)
    } else {
        null
    }
    val time = timestamp ?: System.currentTimeMillis()

    return ServerMetrics(
        cpuPercent = cpu,
        ramPercent = ram,
        swapPercent = swap,
        diskPercent = disk,
        loadAverage = load,
        timestamp = time
    )
}
