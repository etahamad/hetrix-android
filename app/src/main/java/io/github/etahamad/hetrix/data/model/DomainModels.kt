package io.github.etahamad.hetrix.data.model

/**
 * Domain model representing an uptime monitor or monitored server.
 */
data class ServerMonitor(
    val id: String,
    val name: String,
    val type: String,
    val target: String,
    val resolveAddress: String? = null,
    val resolveInfo: String? = null,
    val status: MonitorStatus,
    val uptimePercentage: Double,
    val responseTimeMs: Long?,
    val lastCheckTimestamp: Long,
    val hasAgent: Boolean,
    val locations: List<LocationCheck> = emptyList(),
    val metrics: ServerMetrics? = null
)

/**
 * Domain model representing real-time system performance metrics and hardware info.
 */
data class ServerMetrics(
    val cpuPercent: Float,
    val ramPercent: Float,
    val swapPercent: Float?,
    val diskPercent: Float,
    val loadAverage: String?,
    val timestamp: Long,
    val hostname: String? = null,
    val operatingSystem: String? = null,
    val kernel: String? = null,
    val systemUptimeSeconds: Long? = null,
    val cpuModel: String? = null,
    val cpuCores: Int? = null,
    val cpuThreads: Int? = null,
    val cpuSpeedMhz: Int? = null,
    val ramSizeBytes: Long? = null,
    val swapSizeBytes: Long? = null,
    val diskSizeBytes: Long? = null,
    val diskUsedBytes: Long? = null,
    val diskAvailableBytes: Long? = null,
    val diskMount: String? = null,
    val networkInBps: Long? = null,
    val networkOutBps: Long? = null,
    val networkInterfaceName: String? = null,
    val ipv4Addresses: List<String> = emptyList(),
    val ipv6Addresses: List<String> = emptyList(),
    val openPorts: List<Int> = emptyList(),
    val history: List<TelemetryHistoryPoint> = emptyList()
)

data class TelemetryHistoryPoint(
    val timestamp: Long,
    val cpuPercent: Float,
    val ramPercent: Float,
    val diskPercent: Float,
    val load1: Float?,
    val netInBps: Long?,
    val netOutBps: Long?
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

    val effectiveType = type?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Website"

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
        resolveAddress = resolveAddress,
        resolveInfo = parsedResolveInfo,
        status = parsedStatus,
        uptimePercentage = parsedUptime.coerceIn(0.0, 100.0),
        responseTimeMs = averageLocationPing,
        lastCheckTimestamp = parsedLastCheck,
        hasAgent = agentPresent,
        locations = parsedLocations,
        metrics = overrideMetrics
    )
}

/**
 * Maps full [ServerAgentMetricsResponseDto] payload into rich domain [ServerMetrics].
 */
fun ServerAgentMetricsResponseDto.toDomain(): ServerMetrics? {
    val latest = allStats.firstOrNull()
    if (latest == null && cpu == null && memory == null && disk == null) {
        return null
    }

    val primaryDisk = disk?.disks?.firstOrNull()
    val primaryNic = networkInterfaces.firstOrNull()

    val cpuVal = (latest?.cpuUsage ?: 0.0).toFloat().coerceIn(0f, 100f)
    val ramVal = (latest?.ramUsage ?: 0.0).toFloat().coerceIn(0f, 100f)
    val swapVal = latest?.swapUsage?.toFloat()?.coerceIn(0f, 100f)
    val diskVal = (primaryDisk?.usagePercent ?: latest?.diskUsage ?: 0.0).toFloat().coerceIn(0f, 100f)

    val loadStr = if (latest?.load1 != null) {
        String.format(java.util.Locale.US, "%.2f %.2f %.2f", latest.load1, latest.load5 ?: latest.load1, latest.load15 ?: latest.load1)
    } else null

    val historyPoints = allStats.take(30).reversed().map { pt ->
        TelemetryHistoryPoint(
            timestamp = pt.timestamp ?: System.currentTimeMillis(),
            cpuPercent = (pt.cpuUsage ?: 0.0).toFloat().coerceIn(0f, 100f),
            ramPercent = (pt.ramUsage ?: 0.0).toFloat().coerceIn(0f, 100f),
            diskPercent = (pt.diskUsage ?: 0.0).toFloat().coerceIn(0f, 100f),
            load1 = pt.load1?.toFloat(),
            netInBps = pt.netIn,
            netOutBps = pt.netOut
        )
    }

    return ServerMetrics(
        cpuPercent = cpuVal,
        ramPercent = ramVal,
        swapPercent = swapVal,
        diskPercent = diskVal,
        loadAverage = loadStr,
        timestamp = latest?.timestamp ?: meta?.lastUpdated ?: System.currentTimeMillis(),
        hostname = system?.hostname,
        operatingSystem = system?.operatingSystem,
        kernel = system?.kernel,
        systemUptimeSeconds = system?.uptime,
        cpuModel = cpu?.model,
        cpuCores = cpu?.cores,
        cpuThreads = cpu?.threads,
        cpuSpeedMhz = cpu?.speedMhz,
        ramSizeBytes = memory?.ramSizeBytes,
        swapSizeBytes = memory?.swapSizeBytes,
        diskSizeBytes = primaryDisk?.sizeBytes ?: disk?.totalSizeBytes,
        diskUsedBytes = primaryDisk?.usedBytes,
        diskAvailableBytes = primaryDisk?.availableBytes,
        diskMount = primaryDisk?.mount,
        networkInBps = primaryNic?.netInBps ?: latest?.netIn,
        networkOutBps = primaryNic?.netOutBps ?: latest?.netOut,
        networkInterfaceName = primaryNic?.name,
        ipv4Addresses = primaryNic?.ipv4.orEmpty(),
        ipv6Addresses = primaryNic?.ipv6.orEmpty(),
        openPorts = portConnections.mapNotNull { it.port },
        history = historyPoints
    )
}

/**
 * Maps legacy or single [AgentMetricsPointDto] to [ServerMetrics] domain model.
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

/**
 * Domain model representing a Blacklist and Microsoft SNDS monitored asset.
 */
data class BlacklistMonitor(
    val id: String,
    val name: String,
    val target: String,
    val type: String,
    val listedCount: Int,
    val totalRbls: Int,
    val status: ReputationStatus,
    val sndsStatus: SndsStatus,
    val lastCheckTimestamp: Long,
    val reportUrl: String?,
    val delistUrls: List<String> = emptyList()
)

enum class ReputationStatus(val displayName: String) {
    LISTED("Listed"),
    WARNING("Warning"),
    UNKNOWN("Unknown"),
    CLEAN("Clean")
}

enum class SndsStatus(val displayName: String) {
    CLEAN("Clean"),
    WARNING("Warning"),
    NOT_AVAILABLE("Not available")
}

/**
 * Maps [BlacklistMonitorDto] to clean [BlacklistMonitor] domain model.
 */
fun BlacklistMonitorDto.toDomain(): BlacklistMonitor {
    val effectiveId = id ?: "bl_${target?.hashCode() ?: hashCode()}"
    val effectiveName = name?.takeIf { it.isNotBlank() }
        ?: target?.takeIf { it.isNotBlank() }
        ?: "Asset $effectiveId"
    val effectiveTarget = target ?: "N/A"
    val effectiveType = type?.uppercase() ?: "IP / DOMAIN"

    val listCount = listedCount ?: listedRbls ?: listed.size
    val totalCount = totalRbls ?: 32

    val repStatus = when {
        listCount > 0 -> ReputationStatus.LISTED
        rblStatus?.equals("listed", ignoreCase = true) == true || blacklistStatus?.equals("listed", ignoreCase = true) == true -> ReputationStatus.LISTED
        rblStatus?.equals("warning", ignoreCase = true) == true || blacklistStatus?.equals("warning", ignoreCase = true) == true -> ReputationStatus.WARNING
        rblStatus?.equals("clean", ignoreCase = true) == true || blacklistStatus?.equals("clean", ignoreCase = true) == true -> ReputationStatus.CLEAN
        listCount == 0 -> ReputationStatus.CLEAN
        else -> ReputationStatus.UNKNOWN
    }

    val parsedSnds = when {
        sndsStatus?.equals("clean", ignoreCase = true) == true || sndsIpStatus?.equals("clean", ignoreCase = true) == true -> SndsStatus.CLEAN
        sndsStatus?.equals("warning", ignoreCase = true) == true || sndsIpStatus?.equals("warning", ignoreCase = true) == true -> SndsStatus.WARNING
        else -> SndsStatus.NOT_AVAILABLE
    }

    val delists = listed.mapNotNull { it.delistUrl }.filter { it.isNotBlank() }

    return BlacklistMonitor(
        id = effectiveId,
        name = effectiveName,
        target = effectiveTarget,
        type = effectiveType,
        listedCount = listCount,
        totalRbls = totalCount,
        status = repStatus,
        sndsStatus = parsedSnds,
        lastCheckTimestamp = lastCheck ?: System.currentTimeMillis(),
        reportUrl = reportUrl ?: "https://hetrixtools.com/dashboard/blacklist-monitors/",
        delistUrls = delists
    )
}
