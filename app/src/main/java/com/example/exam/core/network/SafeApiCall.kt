package com.example.exam.core.network

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a network call safely, translating raw network and HTTP exceptions
 * into structured [ApiResult] instances.
 *
 * Crucial: Re-throws [CancellationException] so Kotlin coroutine cancellation
 * propagates properly without getting swallowed into an error state.
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): ApiResult<T> {
    return try {
        val result = apiCall()
        ApiResult.Success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnknownHostException) {
        ApiResult.Error(
            message = "Unable to resolve host. Check your internet connection.",
            code = null,
            throwable = NetworkException.NoInternetException(cause = e)
        )
    } catch (e: SocketTimeoutException) {
        ApiResult.Error(
            message = "Connection timed out. Please try again.",
            code = null,
            throwable = NetworkException.TimeoutException(cause = e)
        )
    } catch (e: HttpException) {
        val code = e.code()
        val errorMsg = when (code) {
            401 -> "Unauthorized. Please authenticate."
            403 -> "Forbidden. Access denied."
            404 -> "Resource not found (404)."
            in 500..599 -> "Internal server error ($code)."
            else -> "Network request failed with HTTP $code."
        }
        ApiResult.Error(
            message = errorMsg,
            code = code,
            throwable = NetworkException.ServerException(code, errorMsg)
        )
    } catch (e: IOException) {
        ApiResult.Error(
            message = e.localizedMessage ?: "Network I/O error occurred.",
            code = null,
            throwable = NetworkException.UnknownNetworkException(cause = e)
        )
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.localizedMessage ?: "Unexpected error occurred.",
            code = null,
            throwable = e
        )
    }
}

