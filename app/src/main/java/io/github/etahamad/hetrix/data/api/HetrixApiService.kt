package io.github.etahamad.hetrix.data.api

import io.github.etahamad.hetrix.data.model.AgentMetricsApiResponseDto
import io.github.etahamad.hetrix.data.model.AgentMetricsDto
import io.github.etahamad.hetrix.data.model.MonitorDto
import io.github.etahamad.hetrix.data.model.MonitorsApiResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Retrofit REST client interface for HetrixTools v3 API.
 * Base URL: https://api.hetrixtools.com/v3/
 */
interface HetrixApiService {

    /**
     * Retrieves all uptime and heartbeat monitors registered to the account.
     */
    @GET("uptime/monitors")
    suspend fun getMonitors(
        @Header("Authorization") authOverride: String? = null
    ): Response<List<MonitorDto>>

    /**
     * Fallback endpoint if monitors are wrapped inside a JSON response object.
     */
    @GET("uptime/monitors")
    suspend fun getMonitorsEnvelope(
        @Header("Authorization") authOverride: String? = null
    ): Response<MonitorsApiResponseDto>

    /**
     * Retrieves real-time server agent performance metrics for a specific monitor.
     */
    @GET("uptime/monitors/{monitor_id}/server-agent-metrics")
    suspend fun getServerAgentMetrics(
        @Path("monitor_id") monitorId: String,
        @Header("Authorization") authOverride: String? = null
    ): Response<AgentMetricsDto>

    /**
     * Fallback endpoint if agent metrics are wrapped inside a JSON response object.
     */
    @GET("uptime/monitors/{monitor_id}/server-agent-metrics")
    suspend fun getServerAgentMetricsEnvelope(
        @Path("monitor_id") monitorId: String,
        @Header("Authorization") authOverride: String? = null
    ): Response<AgentMetricsApiResponseDto>
}
