package com.example.exam.feature.api.presentation

import com.example.exam.data.remote.model.MovieDetailsDto
import com.example.exam.data.remote.model.MovieDto

enum class MovieCategory(val displayName: String) {
    TRENDING("Trending"),
    POPULAR("Popular"),
    TOP_RATED("Top Rated"),
    UPCOMING("Upcoming"),
    FAVOURITES("Favourites")
}

enum class SearchMode {
    MOVIES,
    ARTISTS
}

data class ApiUiState(
    val selectedCategory: MovieCategory = MovieCategory.TRENDING,
    val movies: List<MovieDto> = emptyList(),
    val favouriteIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.MOVIES,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val selectedMovieDetail: MovieDetailsDto? = null,
    val isLoadingDetail: Boolean = false,
    val detailErrorMessage: String? = null
)
