package io.github.etahamad.hetrix.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Health check response from GET /v3/ping.
 */
@Serializable
data class PingResponseDto(
    @SerialName("status")
    val status: String? = null,

    @SerialName("message")
    val message: String? = null
)

/**
 * Top-level response for GET /v3/uptime-monitors.
 */
@Serializable
data class UptimeMonitorsResponseDto(
    @SerialName("status")
    val status: String? = null,

    @SerialName("monitors")
    val monitors: List<MonitorDto> = emptyList(),

    @SerialName("data")
    val data: List<MonitorDto> = emptyList(),

    @SerialName("message")
    val message: String? = null
)

/**
 * Location check latency and status entry.
 */
data class LocationCheck(
    val locationName: String,
    val status: MonitorStatus,
    val responseTimeMs: Long?
)

/**
 * Data Transfer Object representing an individual monitor in HetrixTools v3 API.
 */
@Serializable
data class MonitorDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("SID")
    val sid: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("label")
    val label: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("target")
    val target: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("resolve_address")
    val resolveAddress: String? = null,

    @SerialName("resolve_address_info")
    val resolveAddressInfo: JsonElement? = null,

    @SerialName("ip_or_host")
    val ipOrHost: String? = null,

    @SerialName("uptime_status")
    val uptimeStatus: String? = null,

    @SerialName("monitor_status")
    val monitorStatus: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("uptime")
    val uptimeElement: JsonElement? = null,

    @SerialName("uptime_ratio")
    val uptimeRatio: Double? = null,

    @SerialName("response_time")
    val responseTimeMs: Long? = null,

    @SerialName("locations")
    val locationsElement: JsonElement? = null,

    @SerialName("last_check")
    val lastCheckTimestamp: Long? = null,

    @SerialName("last_status_change")
    val lastStatusChangeTimestamp: Long? = null,

    @SerialName("created_at")
    val createdAt: Long? = null,

    @SerialName("has_agent")
    val hasAgent: Boolean? = null,

    @SerialName("agent_installed")
    val agentInstalled: Boolean? = null,

    @SerialName("agent_id")
    val agentId: String? = null
) {
    /**
     * Extracts uptime percentage as a Double safely from string or number JSON primitive.
     */
    val parsedUptime: Double?
        get() = try {
            uptimeElement?.jsonPrimitive?.content?.toDoubleOrNull()
        } catch (_: Exception) {
            null
        }

    /**
     * Parses locations map dynamically (handles both JSON object and empty array).
     */
    val parsedLocations: List<LocationCheck>
        get() = try {
            val jsonObject = locationsElement?.jsonObject ?: return emptyList()
            jsonObject.entries.mapNotNull { (locationKey, value) ->
                try {
                    val locObj = value.jsonObject
                    val locStatus = locObj["uptime_status"]?.jsonPrimitive?.content
                    val ping = locObj["response_time"]?.jsonPrimitive?.content?.toLongOrNull()
                    LocationCheck(
                        locationName = locationKey.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        status = MonitorStatus.fromString(locStatus),
                        responseTimeMs = ping
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }

    /**
     * Calculates average response time from location checks if available.
     */
    val averageLocationPing: Long?
        get() {
            val validLatencies = parsedLocations.mapNotNull { it.responseTimeMs }
            return if (validLatencies.isNotEmpty()) {
                validLatencies.average().toLong()
            } else {
                responseTimeMs
            }
        }

    /**
     * Extracts ISP and Location string from resolve_address_info if available.
     */
    val parsedResolveInfo: String?
        get() = try {
            val obj = resolveAddressInfo?.jsonObject ?: return null
            val isp = obj["ISP"]?.jsonPrimitive?.content
            val city = obj["City"]?.jsonPrimitive?.content
            val country = obj["Country"]?.jsonPrimitive?.content
            listOfNotNull(isp, city, country).filter { it.isNotBlank() }.joinToString(", ")
        } catch (_: Exception) {
            null
        }
}

/**
 * Top-level response for GET /v3/uptime-monitors/{monitor_id}/server-agent/metrics.
 */
@Serializable
data class ServerAgentMetricsResponseDto(
    @SerialName("status")
    val status: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("agent")
    val agent: AgentInfoDto? = null,

    @SerialName("system")
    val system: SystemInfoDto? = null,

    @SerialName("cpu")
    val cpu: CpuInfoDto? = null,

    @SerialName("memory")
    val memory: MemoryInfoDto? = null,

    @SerialName("disk")
    val disk: DiskContainerDto? = null,

    @SerialName("network_interfaces")
    val networkInterfaces: List<NetworkInterfaceDto> = emptyList(),

    @SerialName("port_connections")
    val portConnections: List<PortConnectionDto> = emptyList(),

    @SerialName("stats")
    val stats: List<AgentMetricsPointDto> = emptyList(),

    @SerialName("metrics")
    val legacyMetrics: List<AgentMetricsPointDto> = emptyList(),

    @SerialName("data")
    val legacyData: List<AgentMetricsPointDto> = emptyList(),

    @SerialName("meta")
    val meta: MetricsMetaDto? = null
) {
    val allStats: List<AgentMetricsPointDto>
        get() = when {
            stats.isNotEmpty() -> stats
            legacyMetrics.isNotEmpty() -> legacyMetrics
            legacyData.isNotEmpty() -> legacyData
            else -> emptyList()
        }
}

@Serializable
data class AgentInfoDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("version")
    val version: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("ip_address")
    val ipAddress: String? = null,

    @SerialName("date_added")
    val dateAdded: Long? = null
)

@Serializable
data class SystemInfoDto(
    @SerialName("hostname")
    val hostname: String? = null,

    @SerialName("operating_system")
    val operatingSystem: String? = null,

    @SerialName("kernel")
    val kernel: String? = null,

    @SerialName("uptime")
    val uptime: Long? = null,

    @SerialName("reboot_required")
    val rebootRequired: Boolean? = null
)

@Serializable
data class CpuInfoDto(
    @SerialName("model")
    val model: String? = null,

    @SerialName("speed")
    val speedMhz: Int? = null,

    @SerialName("sockets")
    val sockets: Int? = null,

    @SerialName("cores")
    val cores: Int? = null,

    @SerialName("threads")
    val threads: Int? = null
)

@Serializable
data class MemoryInfoDto(
    @SerialName("ram_size")
    val ramSizeBytes: Long? = null,

    @SerialName("swap_size")
    val swapSizeBytes: Long? = null
)

@Serializable
data class DiskContainerDto(
    @SerialName("total_size")
    val totalSizeBytes: Long? = null,

    @SerialName("disks")
    val disks: List<SingleDiskDto> = emptyList()
)

@Serializable
data class SingleDiskDto(
    @SerialName("mount")
    val mount: String? = null,

    @SerialName("size")
    val sizeBytes: Long? = null,

    @SerialName("used")
    val usedBytes: Long? = null,

    @SerialName("available")
    val availableBytes: Long? = null,

    @SerialName("usage_percent")
    val usagePercent: Double? = null,

    @SerialName("io_read")
    val ioRead: Long? = null,

    @SerialName("io_write")
    val ioWrite: Long? = null,

    @SerialName("inodes")
    val inodes: Long? = null,

    @SerialName("inodes_used")
    val inodesUsed: Long? = null
)

@Serializable
data class NetworkInterfaceDto(
    @SerialName("name")
    val name: String? = null,

    @SerialName("net_in")
    val netInBps: Long? = null,

    @SerialName("net_out")
    val netOutBps: Long? = null,

    @SerialName("ipv4")
    val ipv4: List<String> = emptyList(),

    @SerialName("ipv6")
    val ipv6: List<String> = emptyList()
)

@Serializable
data class PortConnectionDto(
    @SerialName("port")
    val port: Int? = null,

    @SerialName("connections")
    val connections: Int? = null
)

@Serializable
data class MetricsMetaDto(
    @SerialName("last_updated")
    val lastUpdated: Long? = null
)

/**
 * Telemetry metrics data point.
 */
@Serializable
data class AgentMetricsPointDto(
    @SerialName("timestamp")
    val timestamp: Long? = null,

    @SerialName("cpu")
    val cpuUsage: Double? = null,

    @SerialName("iowait")
    val ioWait: Double? = null,

    @SerialName("steal")
    val steal: Double? = null,

    @SerialName("user")
    val user: Double? = null,

    @SerialName("system")
    val system: Double? = null,

    @SerialName("cpu_temp")
    val cpuTemp: Double? = null,

    @SerialName("load_1")
    val load1: Double? = null,

    @SerialName("load_5")
    val load5: Double? = null,

    @SerialName("load_15")
    val load15: Double? = null,

    @SerialName("ram")
    val ramUsage: Double? = null,

    @SerialName("swap")
    val swapUsage: Double? = null,

    @SerialName("buffered")
    val buffered: Double? = null,

    @SerialName("cached")
    val cached: Double? = null,

    @SerialName("disk")
    val diskUsage: Double? = null,

    @SerialName("net_in")
    val netIn: Long? = null,

    @SerialName("net_out")
    val netOut: Long? = null
)

/**
 * Error payload returned by the HetrixTools API.
 */
@Serializable
data class HetrixApiErrorDto(
    @SerialName("status")
    val status: String? = null,

    @SerialName("error")
    val error: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("error_message")
    val errorMessage: String? = null
)

/**
 * Top-level response for GET /v3/blacklist-monitors.
 */
@Serializable
data class BlacklistMonitorsResponseDto(
    @SerialName("monitors")
    val monitors: List<BlacklistMonitorDto> = emptyList(),

    @SerialName("data")
    val data: List<BlacklistMonitorDto> = emptyList(),

    @SerialName("meta")
    val meta: JsonElement? = null
)

/**
 * Individual Blacklist / SNDS monitor item from API.
 */
@Serializable
data class BlacklistMonitorDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("target")
    val target: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("rbl_status")
    val rblStatus: String? = null,

    @SerialName("blacklist_status")
    val blacklistStatus: String? = null,

    @SerialName("listed_count")
    val listedCount: Int? = null,

    @SerialName("listed_rbls")
    val listedRbls: Int? = null,

    @SerialName("total_rbls")
    val totalRbls: Int? = null,

    @SerialName("snds_status")
    val sndsStatus: String? = null,

    @SerialName("snds_ip_status")
    val sndsIpStatus: String? = null,

    @SerialName("last_check")
    val lastCheck: Long? = null,

    @SerialName("report_id")
    val reportId: String? = null,

    @SerialName("report_url")
    val reportUrl: String? = null,

    @SerialName("listed")
    val listed: List<BlacklistListedItemDto> = emptyList()
)

@Serializable
data class BlacklistListedItemDto(
    @SerialName("rbl")
    val rblName: String? = null,

    @SerialName("delist")
    val delistUrl: String? = null
)
