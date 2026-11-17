package com.example.exam.data.remote.model

import com.example.exam.core.common.Constants
import com.google.gson.annotations.SerializedName

data class MovieDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("original_title")
    val originalTitle: String?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double?,
    @SerializedName("vote_count")
    val voteCount: Int?,
    @SerializedName("popularity")
    val popularity: Double?,
    @SerializedName("original_language")
    val originalLanguage: String?
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "${Constants.TMDB_IMAGE_BASE_W500}$it" }

    val fullBackdropUrl: String?
        get() = backdropPath?.let { "${Constants.TMDB_IMAGE_BASE_ORIGINAL}$it" }

    val releaseYear: String
        get() = releaseDate?.take(4) ?: "N/A"

    val formattedRating: String
        get() = voteAverage?.let { String.format("%.1f", it) } ?: "N/A"
}

data class MovieResponseDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<MovieDto>?,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

