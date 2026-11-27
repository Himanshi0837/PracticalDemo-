package com.example.exam.feature.api.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exam.core.network.ApiResult
import com.example.exam.data.remote.model.MovieDto
import com.example.exam.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiUiState())
    val uiState: StateFlow<ApiUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        // Collect favourite IDs reactive updates
        viewModelScope.launch {
            repository.favouriteIds.collect { ids ->
                _uiState.update { it.copy(favouriteIds = ids) }
                if (_uiState.value.selectedCategory == MovieCategory.FAVOURITES) {
                    _uiState.update { it.copy(movies = repository.favouriteMovies.value) }
                }
            }
        }

        loadMovies(page = 1)
    }

    fun selectCategory(category: MovieCategory) {

        if (
            _uiState.value.selectedCategory == category &&
            _uiState.value.searchQuery.isEmpty()
        ) return

        searchDebounceJob?.cancel()

        _uiState.update {
            it.copy(
                selectedCategory = category,
                searchQuery = "",
                currentPage = 1,
                errorMessage = null,
                isLoading = category != MovieCategory.FAVOURITES
            )
        }

        if (category == MovieCategory.FAVOURITES) {

            _uiState.update {
                it.copy(
                    movies = repository.favouriteMovies.value,
                    isLoading = false,
                    totalPages = 1
                )
            }

        } else {
            loadMovies(page = 1)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchDebounceJob?.cancel()
        if (query.isBlank()) {
            if (_uiState.value.selectedCategory == MovieCategory.FAVOURITES) {
                _uiState.update { it.copy(movies = repository.favouriteMovies.value) }
            } else {
                loadMovies(page = 1)
            }
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            performSearch(query.trim(), page = 1)
        }
    }

    fun setSearchMode(mode: SearchMode) {
        if (_uiState.value.searchMode == mode) return
        _uiState.update { it.copy(searchMode = mode) }
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            performSearch(query.trim(), page = 1)
        }
    }

    fun loadMovies(page: Int = 1) {
        if (_uiState.value.selectedCategory == MovieCategory.FAVOURITES) return

        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isPaginationLoading = true) }
            }

            val result = when (_uiState.value.selectedCategory) {
                MovieCategory.TRENDING -> repository.getTrendingMovies(page)
                MovieCategory.POPULAR -> repository.getPopularMovies(page)
                MovieCategory.TOP_RATED -> repository.getTopRatedMovies(page)
                MovieCategory.UPCOMING -> repository.getUpcomingMovies(page)
                MovieCategory.FAVOURITES -> return@launch
            }

            when (result) {
                is ApiResult.Success -> {
                    val newMovies = result.data.results ?: emptyList()
                    _uiState.update { current ->
                        current.copy(
                            movies = if (page == 1) newMovies else current.movies + newMovies,
                            currentPage = page,
                            totalPages = result.data.totalPages,
                            isLoading = false,
                            isPaginationLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPaginationLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResult.Loading -> {}
            }
        }
    }

    private fun performSearch(query: String, page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isPaginationLoading = true) }
            }

            if (_uiState.value.searchMode == SearchMode.ARTISTS) {
                when (val result = repository.searchByArtist(query)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                movies = result.data,
                                currentPage = 1,
                                totalPages = 1,
                                isLoading = false,
                                isPaginationLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPaginationLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    ApiResult.Loading -> {}
                }
            } else {
                when (val result = repository.searchMovies(query, page)) {
                    is ApiResult.Success -> {
                        val newMovies = result.data.results ?: emptyList()
                        _uiState.update { current ->
                            current.copy(
                                movies = if (page == 1) newMovies else current.movies + newMovies,
                                currentPage = page,
                                totalPages = result.data.totalPages,
                                isLoading = false,
                                isPaginationLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPaginationLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    ApiResult.Loading -> {}
                }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isPaginationLoading || state.currentPage >= state.totalPages) return
        val nextPage = state.currentPage + 1

        if (state.searchQuery.isNotBlank()) {
            if (state.searchMode == SearchMode.MOVIES) {
                performSearch(state.searchQuery, nextPage)
            }
        } else {
            loadMovies(nextPage)
        }
    }

    fun toggleFavourite(movie: MovieDto) {
        repository.toggleFavourite(movie)
    }

    fun openMovieDetail(movieId: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingDetail = true,
                    selectedMovieDetail = null,
                    detailErrorMessage = null
                )
            }

            when (val result = repository.getMovieDetails(movieId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedMovieDetail = result.data,
                            isLoadingDetail = false,
                            detailErrorMessage = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            detailErrorMessage = result.message
                        )
                    }
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun closeMovieDetail() {
        _uiState.update {
            it.copy(
                selectedMovieDetail = null,
                isLoadingDetail = false,
                detailErrorMessage = null
            )
        }
    }

    fun retry() {
        if (_uiState.value.searchQuery.isNotBlank()) {
            performSearch(_uiState.value.searchQuery, _uiState.value.currentPage)
        } else {
            loadMovies(_uiState.value.currentPage)
        }
    }
}
