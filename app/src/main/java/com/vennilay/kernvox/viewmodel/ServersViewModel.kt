package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.HubOverview
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.network.HttpClientFactory
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.ServersRepository
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.net.URI

class ServersViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val settingsRepository = AppSettingsRepository(application)
    private lateinit var serversRepository: ServersRepository

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
                        val baseUrl = settings.serverUrl.trimEnd('/')
                        val httpClient = HttpClientFactory.create(baseUrl, settings.apiKey)
                        val apiService = KernvoxApiService(httpClient)
                        serversRepository = ApiServersRepository(apiService)
                        _hubOverview.value = buildHubOverview(baseUrl, _servers.value)
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
                _hubOverview.value = buildHubOverview(currentSettings.serverUrl.trimEnd('/'), serversList)
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

    private fun buildHubOverview(baseUrl: String, servers: List<Server>): HubOverview {
        val parsedUri = runCatching { URI(baseUrl) }.getOrNull()
        val host = parsedUri?.host ?: baseUrl.removePrefix("https://").removePrefix("http://")
        val port = parsedUri?.port?.takeIf { it >= 0 }
        val availableNodes = servers.count { it.isAvailable == true }
        val lastUpdate = servers.mapNotNull { it.lastMetricTimestamp }.maxOrNull()

        return HubOverview(
            name = "KernvoxHub",
            baseUrl = baseUrl,
            host = host,
            port = port,
            totalNodes = servers.size,
            availableNodes = availableNodes,
            lastUpdate = lastUpdate,
        )
    }
}
