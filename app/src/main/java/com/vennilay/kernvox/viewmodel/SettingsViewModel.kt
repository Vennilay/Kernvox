package com.vennilay.kernvox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.network.ServerUrlValidator
import com.vennilay.kernvox.data.repository.toUserFriendlyMessageRes
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.data.storage.AutoLockTimeout
import com.vennilay.kernvox.data.storage.ThemeMode
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.state.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _settingsState = MutableStateFlow<UiState<AppSettings>>(UiState.Loading)
    val settingsState: StateFlow<UiState<AppSettings>> = _settingsState.asStateFlow()

    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val messages: SharedFlow<UiText> = _messages.asSharedFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                _settingsState.value = UiState.Success(settings)
            } catch (e: Exception) {
                _settingsState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes()))
            }
        }
    }

    fun saveSettings(serverUrl: String, apiKey: String, actionKey: String) {
        viewModelScope.launch {
            val previousSettings = (_settingsState.value as? UiState.Success)?.data
            try {
                if (!ServerUrlValidator.isAllowed(serverUrl)) {
                    _settingsState.value = UiState.Error(UiText.resource(R.string.error_release_https_required))
                    return@launch
                }
                _settingsState.value = UiState.Loading
                settingsRepository.saveSettings(serverUrl, apiKey, actionKey)
                _settingsState.value = UiState.Success(
                    AppSettings(
                        serverUrl = serverUrl,
                        apiKey = apiKey,
                        actionKey = actionKey,
                        hasSeenWelcome = previousSettings?.hasSeenWelcome ?: false,
                        themeMode = previousSettings?.themeMode ?: ThemeMode.SYSTEM,
                        autoLockTimeout = previousSettings?.autoLockTimeout ?: AutoLockTimeout.FIVE_MINUTES,
                        isPasswordLockEnabled = previousSettings?.isPasswordLockEnabled ?: false,
                        isBiometricUnlockEnabled = previousSettings?.isBiometricUnlockEnabled ?: false,
                        isPrivacyModeEnabled = previousSettings?.isPrivacyModeEnabled ?: false,
                    )
                )
            } catch (e: Exception) {
                _settingsState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes()))
            }
        }
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        updateSettings { settings ->
            settingsRepository.saveThemeMode(themeMode)
            settings.copy(themeMode = themeMode)
        }
    }

    fun saveAutoLockTimeout(timeout: AutoLockTimeout) {
        updateSettings { settings ->
            settingsRepository.saveAutoLockTimeout(timeout)
            settings.copy(autoLockTimeout = timeout)
        }
    }

    fun resetWelcome() {
        updateSettings { settings ->
            settingsRepository.resetWelcome()
            settings.copy(hasSeenWelcome = false)
        }
    }

    fun enablePassword(password: String) {
        updateSettings { settings ->
            settingsRepository.setPassword(password)
            settings.copy(isPasswordLockEnabled = true)
        }
    }

    fun disablePassword(currentPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val verified = settingsRepository.verifyPassword(currentPassword)
            if (verified) {
                updateSettings { settings ->
                    settingsRepository.clearPassword()
                    settings.copy(
                        isPasswordLockEnabled = false,
                        isBiometricUnlockEnabled = false,
                    )
                }
            }
            onResult(verified)
        }
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        updateSettings { settings ->
            if (enabled && !settings.isPasswordLockEnabled) {
                _messages.emit(UiText.resource(R.string.settings_biometric_requires_password))
                return@updateSettings settings.copy(isBiometricUnlockEnabled = false)
            }
            settingsRepository.setBiometricUnlockEnabled(enabled)
            _messages.emit(
                UiText.resource(
                    if (enabled) {
                        R.string.settings_biometric_enabled
                    } else {
                        R.string.settings_biometric_disabled
                    },
                ),
            )
            settings.copy(isBiometricUnlockEnabled = enabled)
        }
    }

    fun notifyBiometricEnableRejected() {
        _messages.tryEmit(UiText.resource(R.string.settings_biometric_not_enabled))
    }

    fun setPrivacyModeEnabled(enabled: Boolean) {
        updateSettings { settings ->
            settingsRepository.setPrivacyModeEnabled(enabled)
            settings.copy(isPrivacyModeEnabled = enabled)
        }
    }

    private fun updateSettings(block: suspend (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = (_settingsState.value as? UiState.Success)?.data
                ?: settingsRepository.settings.first()
            try {
                _settingsState.value = UiState.Success(block(current))
            } catch (e: Exception) {
                _settingsState.value = UiState.Error(UiText.resource(R.string.error_request_failed))
            }
        }
    }
}
