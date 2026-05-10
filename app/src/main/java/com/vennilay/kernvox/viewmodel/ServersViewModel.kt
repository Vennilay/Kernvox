package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.HubOverview
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.toHubOverview
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.RepositoryFactory
import com.vennilay.kernvox.data.repository.toUserFriendlyMessageRes
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.state.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServersViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val settingsRepository = AppSettingsRepository(application)
    private var serversRepository: ApiServersRepository? = null

    private val _uiState = MutableStateFlow<UiState<List<Server>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Server>>> = _uiState.asStateFlow()

    // Derived from uiState — единственный источник истины, нет рассинхронизации
    val servers: StateFlow<List<Server>> = _uiState
        .map { (it as? UiState.Success)?.data.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _hubOverview = MutableStateFlow<HubOverview?>(null)
    val hubOverview: StateFlow<HubOverview?> = _hubOverview.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentSettings = AppSettings()
    private var loadJob: Job? = null

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings
                .map { ConnectionSettings(it.serverUrl, it.apiKey, it.actionKey) }
                .distinctUntilChanged()
                .collect { connectionSettings ->
                    currentSettings = currentSettings.copy(
                        serverUrl = connectionSettings.serverUrl,
                        apiKey = connectionSettings.apiKey,
                        actionKey = connectionSettings.actionKey,
                    )
                    val hasConfig = connectionSettings.serverUrl.isNotBlank() &&
                        connectionSettings.apiKey.isNotBlank()

                    if (hasConfig) {
                        loadJob?.cancel()
                        serversRepository?.close()
                        serversRepository = RepositoryFactory.create(currentSettings)
                        loadServers()
                    } else {
                        loadJob?.cancel()
                        serversRepository?.close()
                        serversRepository = null
                        _hubOverview.value = null
                        _isRefreshing.value = false
                        _uiState.value = UiState.Error(
                            UiText.resource(com.vennilay.kernvox.R.string.error_missing_connection_settings),
                        )
                    }
                }
        }
    }

    fun loadServers() {
        val repo = serversRepository ?: return
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            try {
                _isRefreshing.value = true
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Loading
                }
                val serversList = withContext(Dispatchers.IO) {
                    repo.getServers()
                }
                _hubOverview.value =
                    serversList.toHubOverview(currentSettings.serverUrl.trimEnd('/'))
                _uiState.value = UiState.Success(serversList)
            } catch (e: Exception) {
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes()))
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        serversRepository?.close()
    }

    private data class ConnectionSettings(
        val serverUrl: String,
        val apiKey: String,
        val actionKey: String,
    )
}
