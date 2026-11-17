package com.example.exam.data.remote

import retrofit2.http.GET

/**
 * Common Retrofit API service definition.
 * Clean placeholder ready for tomorrow's GET, POST, PUT, PATCH, and DELETE exam endpoints.
 */
interface ApiService {

    // Placeholder endpoint for connectivity validation
    @GET("/")
    suspend fun checkHealth(): Map<String, Any>

    /*
     * Location reserved for tomorrow's exam endpoints:
     *
     * @GET("items")
     * suspend fun getItems(): List<ItemDto>
     *
     * @POST("items")
     * suspend fun createItem(@Body item: CreateItemDto): ItemDto
     *
     * @PUT("items/{id}")
     * suspend fun updateItem(@Path("id") id: String, @Body item: UpdateItemDto): ItemDto
     *
     * @PATCH("items/{id}")
     * suspend fun patchItem(@Path("id") id: String, @Body updates: Map<String, Any>): ItemDto
     *
     * @DELETE("items/{id}")
     * suspend fun deleteItem(@Path("id") id: String): Unit
     */
}

