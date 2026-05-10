package com.vennilay.kernvox.ui.state

import androidx.compose.runtime.Immutable

@Immutable
sealed class UiState<out T> {
    @Immutable
    data object Loading : UiState<Nothing>()

    @Immutable
    data class Success<T>(val data: T) : UiState<T>()

    @Immutable
    data class Error(val message: UiText) : UiState<Nothing>()
}
