package com.example.exam.core.network

import java.io.IOException

sealed class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class NoInternetException(message: String = "No internet connection detected", cause: Throwable? = null) :
        NetworkException(message, cause)

    class TimeoutException(message: String = "Network request timed out", cause: Throwable? = null) :
        NetworkException(message, cause)

    class ServerException(val statusCode: Int, message: String = "Server error occurred (HTTP $statusCode)") :
        NetworkException(message)

    class UnauthorizedException(message: String = "Unauthorized request (HTTP 401)") :
        NetworkException(message)

    class UnknownNetworkException(message: String = "An unexpected network error occurred", cause: Throwable? = null) :
        NetworkException(message, cause)
}

