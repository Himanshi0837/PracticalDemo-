package com.example.exam.core.common

/**
 * Standard presentation state container for Jetpack Compose screens.
 * Distinct from Resource/ApiResult: directly observed as StateFlow by UI composables.
 */
data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) {
    val hasError: Boolean
        get() = error != null

    val isSuccess: Boolean
        get() = !isLoading && error == null && data != null
}

