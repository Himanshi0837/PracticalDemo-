package com.example.exam.feature.api.presentation

data class ApiUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "API Foundation Ready. Endpoints will be implemented tomorrow.",
    val errorMessage: String? = null
)

