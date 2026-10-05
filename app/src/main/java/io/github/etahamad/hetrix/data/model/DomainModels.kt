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
        ?: url?.takeIf { it.isNotBlank() }
        ?: ipOrHost?.takeIf { it.isNotBlank() }
        ?: "N/A"

    val effectiveType = type?.replaceFirstChar { it.uppercase() } ?: "Server"
    val parsedStatus = MonitorStatus.fromString(status)
    val parsedUptime = uptime ?: uptimeRatio?.let { it * 100.0 } ?: 100.0
    val parsedLastCheck = lastCheckTimestamp ?: lastStatusChangeTimestamp ?: System.currentTimeMillis()
    val agentPresent = hasAgent == true || agentInstalled == true || serverAgentMetrics != null

    val effectiveMetrics = overrideMetrics ?: serverAgentMetrics?.toDomain()

    return ServerMonitor(
        id = effectiveId,
        name = effectiveName,
        type = effectiveType,
        target = effectiveTarget,
        status = parsedStatus,
        uptimePercentage = parsedUptime.coerceIn(0.0, 100.0),
        responseTimeMs = responseTimeMs,
        lastCheckTimestamp = parsedLastCheck,
        hasAgent = agentPresent,
        metrics = effectiveMetrics
    )
}

/**
 * Maps [AgentMetricsDto] to [ServerMetrics] domain model.
 */
fun AgentMetricsDto.toDomain(): ServerMetrics {
    val cpu = (cpuUsage ?: cpuUsageAlt ?: 0.0).toFloat().coerceIn(0f, 100f)
    val ram = (ramUsage ?: ramUsageAlt ?: ramUsedPercent ?: 0.0).toFloat().coerceIn(0f, 100f)
    val swap = (swapUsage ?: swapUsageAlt ?: swapUsedPercent)?.toFloat()?.coerceIn(0f, 100f)
    val disk = (diskUsage ?: diskUsageAlt ?: diskUsedPercent ?: 0.0).toFloat().coerceIn(0f, 100f)
    val load = loadAverage?.takeIf { it.isNotBlank() } ?: loadAlt?.takeIf { it.isNotBlank() }
    val time = timestamp ?: updatedAt ?: System.currentTimeMillis()

    return ServerMetrics(
        cpuPercent = cpu,
        ramPercent = ram,
        swapPercent = swap,
        diskPercent = disk,
        loadAverage = load,
        timestamp = time
    )
}
