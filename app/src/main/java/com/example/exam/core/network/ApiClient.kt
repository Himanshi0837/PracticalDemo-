
package com.example.exam.core.network

import com.example.exam.BuildConfig
import com.example.exam.core.common.Constants
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun createOkHttpClient(
        timeoutSeconds: Long = Constants.NETWORK_TIMEOUT_SECONDS,
        isDebug: Boolean = BuildConfig.DEBUG
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)

        // Authentication interceptor
        builder.addInterceptor { chain ->

            val originalRequest = chain.request()

            val authenticatedRequest = originalRequest.newBuilder()
                .header(
                    "Authorization",
                    "Bearer ${Constants.TMDB_ACCESS_TOKEN}"
                )
                .header("Accept", "application/json")
                .build()

            chain.proceed(authenticatedRequest)
        }

        // Debug logging
        if (isDebug) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            }

            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    fun createRetrofit(
        okHttpClient: OkHttpClient,
        baseUrl: String = Constants.TMDB_BASE_URL
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
