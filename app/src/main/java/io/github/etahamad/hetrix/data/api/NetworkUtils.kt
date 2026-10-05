package io.github.etahamad.hetrix.data.api

import io.github.etahamad.hetrix.data.model.HetrixApiErrorDto
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object NetworkUtils {

    const val BASE_URL = "https://api.hetrixtools.com/v3/"

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    fun createOkHttpClient(authInterceptor: AuthInterceptor, isDebug: Boolean = false): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (isDebug) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    fun createApiService(okHttpClient: OkHttpClient, baseUrl: String = BASE_URL): HetrixApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()
            .create(HetrixApiService::class.java)
    }

    /**
     * Executes an API call safely, parsing error bodies and mapping HTTP exceptions.
     */
    inline fun <T> safeApiCall(apiCall: () -> Response<T>): T {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                return response.body() ?: throw NetworkException.ApiException(
                    statusCode = response.code(),
                    message = "Received empty response from HetrixTools server."
                )
            } else {
                throw parseHttpError(response)
            }
        } catch (e: NetworkException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw NetworkException.TimeoutException(cause = e)
        } catch (e: UnknownHostException) {
            throw NetworkException.NoConnectivityException(cause = e)
        } catch (e: ConnectException) {
            throw NetworkException.NoConnectivityException(cause = e)
        } catch (e: IOException) {
            throw NetworkException.ApiException(
                statusCode = -1,
                message = e.localizedMessage ?: "A network communication error occurred.",
                cause = e
            )
        } catch (e: Exception) {
            throw NetworkException.ApiException(
                statusCode = -1,
                message = e.localizedMessage ?: "An unexpected error occurred.",
                cause = e
            )
        }
    }

    fun parseHttpError(response: Response<*>): NetworkException {
        val code = response.code()
        val errorBody = response.errorBody()?.string()

        val parsedErrorMessage = errorBody?.let { body ->
            try {
                val errorDto = jsonConfig.decodeFromString<HetrixApiErrorDto>(body)
                errorDto.error ?: errorDto.errorMessage ?: errorDto.message
            } catch (_: Exception) {
                null
            }
        }

        return when (code) {
            401 -> NetworkException.UnauthorizedException(
                message = parsedErrorMessage ?: "Unauthorized: Invalid or expired HetrixTools API token."
            )
            403 -> NetworkException.ForbiddenException(
                message = parsedErrorMessage ?: "Forbidden: Access to this HetrixTools resource is restricted."
            )
            429 -> {
                val retryAfter = response.headers()["Retry-After"]?.toLongOrNull()
                NetworkException.RateLimitException(
                    retryAfterSeconds = retryAfter,
                    message = parsedErrorMessage ?: "Rate limit exceeded. Please wait before refreshing."
                )
            }
            in 500..599 -> NetworkException.ServerException(
                statusCode = code,
                message = parsedErrorMessage ?: "HetrixTools service is temporarily unavailable (HTTP $code)."
            )
            else -> NetworkException.ApiException(
                statusCode = code,
                message = parsedErrorMessage ?: response.message().takeIf { it.isNotBlank() } ?: "HTTP $code error returned."
            )
        }
    }
}
