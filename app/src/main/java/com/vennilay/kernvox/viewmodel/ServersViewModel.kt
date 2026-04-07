package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.network.HttpClientFactory
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.ServersRepository
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ServersViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val settingsRepository = AppSettingsRepository(application)
    private lateinit var serversRepository: ServersRepository

    private val _uiState = MutableStateFlow<UiState<List<Server>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Server>>> = _uiState.asStateFlow()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    init {
        initRepository()
    }

    private fun initRepository() {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val hasConfig = settings.serverUrl.isNotBlank() && settings.apiKey.isNotBlank()
            _isConfigured.value = hasConfig

            if (hasConfig) {
                // Нормализуем URL: убираем trailing slash
                val baseUrl = settings.serverUrl.trimEnd('/')
                val httpClient = HttpClientFactory.create(baseUrl, settings.apiKey)
                val apiService = KernvoxApiService(httpClient)
                serversRepository = ApiServersRepository(apiService)
                loadServers()
            } else {
                _uiState.value = UiState.Error("Настройки не указаны. Перейдите в Настройки и укажите URL сервера и API-ключ.")
            }
        }
    }

    fun loadServers() {
        if (!_isConfigured.value) return
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val serversList = serversRepository.getServers()
                _servers.value = serversList
                _uiState.value = UiState.Success(serversList)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
