package io.github.etahamad.hetrix.data.api

import io.github.etahamad.hetrix.data.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that dynamically attaches authorization bearer tokens and API headers.
 */
class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "HetriX-Android/1.0")

        // If request doesn't already have an Authorization header (e.g. from token validation check)
        if (originalRequest.header("Authorization") == null) {
            val token = tokenStorage.getToken()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
