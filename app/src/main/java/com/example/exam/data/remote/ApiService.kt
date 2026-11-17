package com.example.exam.data.remote

import com.example.exam.data.remote.model.MovieDetailsDto
import com.example.exam.data.remote.model.MovieResponseDto
import com.example.exam.data.remote.model.PersonMovieCreditsResponseDto
import com.example.exam.data.remote.model.PersonSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB (The Movie Database) REST API interface.
 */
interface ApiService {

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("search/person")
    suspend fun searchPersons(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): PersonSearchResponseDto

    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(
        @Path("person_id") personId: Int
    ): PersonMovieCreditsResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits"
    ): MovieDetailsDto
}
