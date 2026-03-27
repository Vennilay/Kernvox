package com.vennilay.kernvox.ui.state

/**
 * Запечатанное представление состояния UI для экранов приложения.
 * Используется для управления состояниями загрузки, успеха и ошибки.
 */
sealed class UiState<out T> {
    /**
     * Состояние загрузки данных.
     */
    data object Loading : UiState<Nothing>()

    /**
     * Состояние успешной загрузки данных.
     * @param data Загруженные данные
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Состояние ошибки при загрузке данных.
     * @param message Сообщение об ошибке
     */
    data class Error(val message: String) : UiState<Nothing>()
}
