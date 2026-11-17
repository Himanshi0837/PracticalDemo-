
package com.example.exam.core.network

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a network call safely and converts network/API exceptions
 * into structured ApiResult instances.
 *
 * CancellationException must be rethrown so coroutine cancellation
 * works correctly.
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
            message = "No internet connection. Please check your network.",
            throwable = NetworkException.NoInternetException(cause = e)
        )

    } catch (e: SocketTimeoutException) {
        ApiResult.Error(
            message = "Connection timed out. Please try again.",
            throwable = NetworkException.TimeoutException(cause = e)
        )

    } catch (e: HttpException) {

        val code = e.code()

        val errorMessage = when (code) {
            400 -> "Invalid request. Please try again."
            401 -> "Authentication failed."
            403 -> "You don't have permission to access this content."
            404 -> "Requested content was not found."
            408 -> "Request timed out. Please try again."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server is temporarily unavailable."
            else -> "Request failed. Please try again."
        }

        ApiResult.Error(
            message = errorMessage,
            code = code,
            throwable = NetworkException.ServerException(
                code,
                errorMessage
            )
        )

    } catch (e: IOException) {
        ApiResult.Error(
            message = "Network error. Please check your connection.",
            throwable = NetworkException.UnknownNetworkException(cause = e)
        )

    } catch (e: JsonParseException) {
        ApiResult.Error(
            message = "Unable to process server response.",
            throwable = e
        )

    } catch (e: Exception) {
        ApiResult.Error(
            message = "Something went wrong. Please try again.",
            throwable = e
        )
    }
}
