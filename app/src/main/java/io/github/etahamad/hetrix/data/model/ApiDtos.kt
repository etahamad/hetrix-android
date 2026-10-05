package io.github.etahamad.hetrix.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object representing an individual Uptime / Server Monitor in the HetrixTools v3 API.
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

    @SerialName("ip_or_host")
    val ipOrHost: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("uptime")
    val uptime: Double? = null,

    @SerialName("uptime_ratio")
    val uptimeRatio: Double? = null,

    @SerialName("response_time")
    val responseTimeMs: Long? = null,

    @SerialName("last_check")
    val lastCheckTimestamp: Long? = null,

    @SerialName("last_status_change")
    val lastStatusChangeTimestamp: Long? = null,

    @SerialName("has_agent")
    val hasAgent: Boolean? = null,

    @SerialName("agent_installed")
    val agentInstalled: Boolean? = null,

    @SerialName("server_agent_metrics")
    val serverAgentMetrics: AgentMetricsDto? = null
)

/**
 * Data Transfer Object representing telemetry metrics emitted by the HetrixTools Server Agent.
 */
@Serializable
data class AgentMetricsDto(
    @SerialName("cpu")
    val cpuUsage: Double? = null,

    @SerialName("cpu_usage")
    val cpuUsageAlt: Double? = null,

    @SerialName("ram")
    val ramUsage: Double? = null,

    @SerialName("ram_usage")
    val ramUsageAlt: Double? = null,

    @SerialName("ram_used_percent")
    val ramUsedPercent: Double? = null,

    @SerialName("swap")
    val swapUsage: Double? = null,

    @SerialName("swap_usage")
    val swapUsageAlt: Double? = null,

    @SerialName("swap_used_percent")
    val swapUsedPercent: Double? = null,

    @SerialName("disk")
    val diskUsage: Double? = null,

    @SerialName("disk_usage")
    val diskUsageAlt: Double? = null,

    @SerialName("disk_used_percent")
    val diskUsedPercent: Double? = null,

    @SerialName("load_avg")
    val loadAverage: String? = null,

    @SerialName("load")
    val loadAlt: String? = null,

    @SerialName("timestamp")
    val timestamp: Long? = null,

    @SerialName("updated_at")
    val updatedAt: Long? = null
)

/**
 * Top-level response wrapper for monitors list if returned as an envelope.
 */
@Serializable
data class MonitorsApiResponseDto(
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
 * Top-level response wrapper for single agent metrics endpoint.
 */
@Serializable
data class AgentMetricsApiResponseDto(
    @SerialName("status")
    val status: String? = null,

    @SerialName("metrics")
    val metrics: AgentMetricsDto? = null,

    @SerialName("data")
    val data: AgentMetricsDto? = null,

    @SerialName("message")
    val message: String? = null
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
