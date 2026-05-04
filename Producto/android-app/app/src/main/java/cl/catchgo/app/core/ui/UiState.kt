package cl.catchgo.app.core.ui

import cl.catchgo.app.core.error.ApiError

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: ApiError) : UiState<Nothing>
}
