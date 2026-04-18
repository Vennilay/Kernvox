package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.HubOverview
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.toHubOverview
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.RepositoryFactory
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ServersViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val settingsRepository = AppSettingsRepository(application)
    private lateinit var serversRepository: ApiServersRepository

    private val _uiState = MutableStateFlow<UiState<List<Server>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Server>>> = _uiState.asStateFlow()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _hubOverview = MutableStateFlow<HubOverview?>(null)
    val hubOverview: StateFlow<HubOverview?> = _hubOverview.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private var currentSettings = AppSettings()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings
                .distinctUntilChanged()
                .collect { settings ->
                    currentSettings = settings
                    val hasConfig = settings.serverUrl.isNotBlank() && settings.apiKey.isNotBlank()
                    _isConfigured.value = hasConfig

                    if (hasConfig) {
                        serversRepository = RepositoryFactory.create(settings)
                        _hubOverview.value = _servers.value.toHubOverview(settings.serverUrl.trimEnd('/'))
                        loadServers()
                    } else {
                        _servers.value = emptyList()
                        _hubOverview.value = null
                        _isRefreshing.value = false
                        _uiState.value = UiState.Error(
                            "Настройки не указаны. Перейдите в Настройки и укажите URL сервера и API-ключ.",
                        )
                    }
                }
        }
    }

    fun loadServers() {
        if (!_isConfigured.value) return
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                if (_servers.value.isEmpty()) {
                    _uiState.value = UiState.Loading
                }
                val serversList = serversRepository.getServers()
                _servers.value = serversList
                _hubOverview.value = serversList.toHubOverview(currentSettings.serverUrl.trimEnd('/'))
                _uiState.value = UiState.Success(serversList)
            } catch (e: Exception) {
                if (_servers.value.isEmpty()) {
                    _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
