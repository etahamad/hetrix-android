package io.github.etahamad.hetrix.data.api

import io.github.etahamad.hetrix.data.model.BlacklistMonitorsResponseDto
import io.github.etahamad.hetrix.data.model.PingResponseDto
import io.github.etahamad.hetrix.data.model.ServerAgentMetricsResponseDto
import io.github.etahamad.hetrix.data.model.UptimeMonitorsResponseDto
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
     * Pings the HetrixTools API to verify API key validity and service availability.
     */
    @GET("ping")
    suspend fun ping(
        @Header("Authorization") authOverride: String? = null
    ): Response<PingResponseDto>

    /**
     * Retrieves all uptime and heartbeat monitors registered to the account.
     * Official endpoint: GET /v3/uptime-monitors
     */
    @GET("uptime-monitors")
    suspend fun getUptimeMonitors(
        @Header("Authorization") authOverride: String? = null
    ): Response<UptimeMonitorsResponseDto>

    /**
     * Retrieves real-time server agent performance metrics for a specific monitor.
     * Official endpoint: GET /v3/uptime-monitors/{monitor_id}/server-agent/metrics
     */
    @GET("uptime-monitors/{monitor_id}/server-agent/metrics")
    suspend fun getServerAgentMetrics(
        @Path("monitor_id") monitorId: String,
        @Header("Authorization") authOverride: String? = null
    ): Response<ServerAgentMetricsResponseDto>

    /**
     * Retrieves all blacklist and SNDS monitors registered to the account.
     * Official endpoint: GET /v3/blacklist-monitors
     */
    @GET("blacklist-monitors")
    suspend fun getBlacklistMonitors(
        @Header("Authorization") authOverride: String? = null
    ): Response<BlacklistMonitorsResponseDto>
}
