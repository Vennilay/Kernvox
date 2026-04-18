package com.vennilay.kernvox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _settingsState = MutableStateFlow<UiState<AppSettings>>(UiState.Loading)
    val settingsState: StateFlow<UiState<AppSettings>> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.settings.first()
                _settingsState.value = UiState.Success(settings)
            } catch (e: Exception) {
                _settingsState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun saveSettings(serverUrl: String, apiKey: String) {
        viewModelScope.launch {
            try {
                _settingsState.value = UiState.Loading
                settingsRepository.saveSettings(serverUrl, apiKey)
                _settingsState.value = UiState.Success(AppSettings(serverUrl, apiKey))
            } catch (e: Exception) {
                _settingsState.value = UiState.Error(e.message ?: "Не удалось сохранить")
            }
        }
    }
}
