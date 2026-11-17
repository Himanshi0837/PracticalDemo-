package com.example.exam.data.repository

import com.example.exam.core.network.ApiResult
import com.example.exam.core.network.safeApiCall
import com.example.exam.data.local.FavouriteMoviesStore
import com.example.exam.data.remote.ApiService
import com.example.exam.data.remote.model.MovieDetailsDto
import com.example.exam.data.remote.model.MovieDto
import com.example.exam.data.remote.model.MovieResponseDto
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: ApiService,
    private val favouritesStore: FavouriteMoviesStore
) {
    val favouriteMovies: StateFlow<List<MovieDto>> = favouritesStore.favouriteMovies
    val favouriteIds: StateFlow<Set<Int>> = favouritesStore.favouriteIds

    suspend fun getTrendingMovies(page: Int = 1): ApiResult<MovieResponseDto> {
        return safeApiCall { apiService.getTrendingMovies(page) }
    }

    suspend fun getPopularMovies(page: Int = 1): ApiResult<MovieResponseDto> {
        return safeApiCall { apiService.getPopularMovies(page) }
    }

    suspend fun getTopRatedMovies(page: Int = 1): ApiResult<MovieResponseDto> {
        return safeApiCall { apiService.getTopRatedMovies(page) }
    }

    suspend fun getUpcomingMovies(page: Int = 1): ApiResult<MovieResponseDto> {
        return safeApiCall { apiService.getUpcomingMovies(page) }
    }

    suspend fun searchMovies(query: String, page: Int = 1): ApiResult<MovieResponseDto> {
        return safeApiCall { apiService.searchMovies(query, page) }
    }

    suspend fun searchByArtist(query: String): ApiResult<List<MovieDto>> {
        return safeApiCall {
            val personSearch = apiService.searchPersons(query)
            val firstPerson = personSearch.results?.firstOrNull()
            if (firstPerson != null) {
                val credits = apiService.getPersonMovieCredits(firstPerson.id)
                credits.cast ?: firstPerson.knownFor ?: emptyList()
            } else {
                emptyList()
            }
        }
    }

    suspend fun getMovieDetails(movieId: Int): ApiResult<MovieDetailsDto> {
        return safeApiCall { apiService.getMovieDetails(movieId) }
    }

    fun isFavourite(movieId: Int): Boolean {
        return favouritesStore.isFavourite(movieId)
    }

    fun toggleFavourite(movie: MovieDto): Boolean {
        return favouritesStore.toggleFavourite(movie)
    }
}

