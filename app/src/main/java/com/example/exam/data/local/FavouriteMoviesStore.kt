
package com.example.exam.data.local

import android.content.Context
import com.example.exam.data.remote.model.MovieDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteMoviesStore @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {

    private val prefs = context.getSharedPreferences(
        "tmdb_favourites_prefs",
        Context.MODE_PRIVATE
    )

    private companion object {
        const val KEY_FAVOURITES = "key_favourite_movies_json"
    }

    private val _favouriteMovies =
        MutableStateFlow<List<MovieDto>>(emptyList())

    val favouriteMovies: StateFlow<List<MovieDto>> =
        _favouriteMovies.asStateFlow()

    private val _favouriteIds =
        MutableStateFlow<Set<Int>>(emptySet())

    val favouriteIds: StateFlow<Set<Int>> =
        _favouriteIds.asStateFlow()

    init {
        loadFavourites()
    }

    private fun loadFavourites() {
        val json = prefs.getString(KEY_FAVOURITES, null)

        if (json.isNullOrBlank()) return

        try {
            val type = object : TypeToken<List<MovieDto>>() {}.type

            val movies: List<MovieDto> =
                gson.fromJson<List<MovieDto>>(json, type)
                    ?: emptyList()

            updateState(movies.distinctBy { it.id })

        } catch (e: Exception) {
            prefs.edit()
                .remove(KEY_FAVOURITES)
                .apply()

            updateState(emptyList())
        }
    }

    fun isFavourite(movieId: Int): Boolean {
        return movieId in _favouriteIds.value
    }

    @Synchronized
    fun toggleFavourite(movie: MovieDto): Boolean {

        val currentList = _favouriteMovies.value.toMutableList()

        val exists = currentList.any {
            it.id == movie.id
        }

        if (exists) {
            currentList.removeAll {
                it.id == movie.id
            }
        } else {
            currentList.add(0, movie)
        }

        updateState(currentList)

        val json = gson.toJson(currentList)

        prefs.edit()
            .putString(KEY_FAVOURITES, json)
            .apply()

        return !exists
    }

    private fun updateState(movies: List<MovieDto>) {
        _favouriteMovies.value = movies
        _favouriteIds.value = movies.map { it.id }.toSet()
    }
}
