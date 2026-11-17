package com.example.exam.data.remote.model

import com.example.exam.core.common.Constants
import com.google.gson.annotations.SerializedName

data class MovieDetailsDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("tagline")
    val tagline: String?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("runtime")
    val runtime: Int?,
    @SerializedName("vote_average")
    val voteAverage: Double?,
    @SerializedName("vote_count")
    val voteCount: Int?,
    @SerializedName("popularity")
    val popularity: Double?,
    @SerializedName("original_language")
    val originalLanguage: String?,
    @SerializedName("genres")
    val genres: List<GenreDto>?,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompanyDto>?,
    @SerializedName("credits")
    val credits: CreditsDto?
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "${Constants.TMDB_IMAGE_BASE_W500}$it" }

    val fullBackdropUrl: String?
        get() = backdropPath?.let { "${Constants.TMDB_IMAGE_BASE_ORIGINAL}$it" }

    val formattedRuntime: String
        get() = runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        } ?: "N/A"

    val directorName: String
        get() = credits?.crew?.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.name ?: "Unknown"
}

data class GenreDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class ProductionCompanyDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("logo_path")
    val logoPath: String?
)

data class CreditsDto(
    @SerializedName("cast")
    val cast: List<CastMemberDto>?,
    @SerializedName("crew")
    val crew: List<CrewMemberDto>?
)

data class CastMemberDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("character")
    val character: String?,
    @SerializedName("profile_path")
    val profilePath: String?
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { "${Constants.TMDB_IMAGE_BASE_W500}$it" }
}

data class CrewMemberDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("job")
    val job: String?,
    @SerializedName("department")
    val department: String?
)

