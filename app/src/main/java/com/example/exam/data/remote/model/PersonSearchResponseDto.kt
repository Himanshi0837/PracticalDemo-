package com.example.exam.data.remote.model

import com.google.gson.annotations.SerializedName

data class PersonSearchResponseDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<PersonDto>?,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

data class PersonDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("known_for")
    val knownFor: List<MovieDto>?
)

data class PersonMovieCreditsResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("cast")
    val cast: List<MovieDto>?,
    @SerializedName("crew")
    val crew: List<MovieDto>?
)

