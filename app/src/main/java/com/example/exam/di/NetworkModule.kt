
package com.example.exam.di

import com.example.exam.BuildConfig
import com.example.exam.core.common.Constants
import com.example.exam.core.network.ApiClient
import com.example.exam.data.remote.ApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return ApiClient.createOkHttpClient(
            timeoutSeconds = Constants.NETWORK_TIMEOUT_SECONDS,
            isDebug = BuildConfig.DEBUG
        )
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return ApiClient.createRetrofit(
            okHttpClient = okHttpClient,
            baseUrl = Constants.TMDB_BASE_URL
        )
    }

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
