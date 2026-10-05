package io.github.etahamad.hetrix.data.api

import java.io.IOException

/**
 * Base sealed class for all network-related errors encountered in HetriX.
 */
sealed class NetworkException(
    override val message: String,
    override val cause: Throwable? = null
) : IOException(message, cause) {

    /**
     * HTTP 401 Unauthorized: Invalid, missing, or expired API token.
     */
    data class UnauthorizedException(
        override val message: String = "Unauthorized: Invalid or expired HetrixTools API token.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * HTTP 403 Forbidden: Account restrictions, insufficient permissions, or plan limit.
     */
    data class ForbiddenException(
        override val message: String = "Forbidden: Access denied to requested HetrixTools resource.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * HTTP 429 Too Many Requests: HetrixTools rate limits exceeded.
     */
    data class RateLimitException(
        val retryAfterSeconds: Long? = null,
        override val message: String = "Rate Limit Exceeded: Please wait before refreshing again.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * HTTP 5xx Server Error: HetrixTools API is temporarily unavailable or returned a 500/502/503.
     */
    data class ServerException(
        val statusCode: Int,
        override val message: String = "Server Error ($statusCode): HetrixTools API is temporarily unavailable.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * Client-side network timeout (Connection or Read timeout).
     */
    data class TimeoutException(
        override val message: String = "Connection timed out. Please check your internet connection.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * No internet connection or host unreachable.
     */
    data class NoConnectivityException(
        override val message: String = "Unable to connect. Please verify your device has an active internet connection.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * General API failure with a status code and payload error message.
     */
    data class ApiException(
        val statusCode: Int,
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * Missing API token in local secure storage.
     */
    data class MissingTokenException(
        override val message: String = "No API token configured. Please enter your HetrixTools API token in settings.",
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)
}
