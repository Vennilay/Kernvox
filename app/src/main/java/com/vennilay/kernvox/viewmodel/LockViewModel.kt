package com.vennilay.kernvox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LockUiState(
    val isChecking: Boolean = false,
    val error: UiText? = null,
)

/**
 * Отвечает за проверку пароля блокировки приложения, благодаря чему экран блокировки остается исключительно интерфейсным.
 */
class LockViewModel(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    fun unlockWithPassword(password: String, onUnlocked: () -> Unit) {
        if (password.isBlank()) {
            _uiState.value = LockUiState(error = UiText.resource(R.string.lock_empty_password))
            return
        }

        viewModelScope.launch {
            _uiState.value = LockUiState(isChecking = true)
            val isValid = settingsRepository.verifyPassword(password)
            if (isValid) {
                _uiState.value = LockUiState()
                onUnlocked()
            } else {
                _uiState.value = LockUiState(error = UiText.resource(R.string.lock_wrong_password))
            }
        }
    }

    fun showBiometricError(cancelled: Boolean) {
        _uiState.value = LockUiState(
            error = UiText.resource(
                if (cancelled) R.string.lock_biometric_cancelled else R.string.lock_biometric_failed,
            ),
        )
    }
}
