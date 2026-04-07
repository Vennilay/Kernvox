package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.network.HttpClientFactory
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DetailViewModel(
    private val application: Application,
    private val serverId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Server>>(UiState.Loading)
    val uiState: StateFlow<UiState<Server>> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            val settingsRepo = AppSettingsRepository(application)
            val settings = settingsRepo.settings.first()
            val baseUrl = settings.serverUrl.trimEnd('/')
            val httpClient = HttpClientFactory.create(baseUrl, settings.apiKey)
            val apiService = KernvoxApiService(httpClient)
            val repository = ApiServersRepository(apiService)

            loadDetails(repository)

            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30_000)
                    loadDetails(repository)
                }
            }
        }
    }

    private suspend fun loadDetails(repository: ApiServersRepository) {
        try {
            val server = repository.getServerDetails(serverId)
            _uiState.value = UiState.Success(server)
        } catch (e: Exception) {
            if (_uiState.value is UiState.Loading) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val settingsRepo = AppSettingsRepository(application)
            val settings = settingsRepo.settings.first()
            val baseUrl = settings.serverUrl.trimEnd('/')
            val httpClient = HttpClientFactory.create(baseUrl, settings.apiKey)
            val apiService = KernvoxApiService(httpClient)
            val repository = ApiServersRepository(apiService)
            loadDetails(repository)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
