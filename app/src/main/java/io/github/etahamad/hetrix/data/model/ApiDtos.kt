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

    @SerialName("metrics")
    val metrics: List<AgentMetricsPointDto> = emptyList(),

    @SerialName("data")
    val data: List<AgentMetricsPointDto> = emptyList(),

    @SerialName("message")
    val message: String? = null
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

    @SerialName("ram")
    val ramUsage: Double? = null,

    @SerialName("swap")
    val swapUsage: Double? = null,

    @SerialName("disk")
    val diskUsage: Double? = null,

    @SerialName("load_1")
    val load1: Double? = null,

    @SerialName("load_5")
    val load5: Double? = null,

    @SerialName("load_15")
    val load15: Double? = null,

    @SerialName("net_in")
    val netIn: Double? = null,

    @SerialName("net_out")
    val netOut: Double? = null
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
