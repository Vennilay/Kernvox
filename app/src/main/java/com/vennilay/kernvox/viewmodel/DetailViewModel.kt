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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var pollingJob: Job? = null
    private var repository: ApiServersRepository? = null

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
            repository = ApiServersRepository(apiService)

            loadDetails(showLoading = true)

            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30_000)
                    loadDetails(showLoading = false)
                }
            }
        }
    }

    private suspend fun loadDetails(showLoading: Boolean) {
        val currentRepository = repository ?: return
        try {
            if (showLoading && _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            } else {
                _isRefreshing.value = true
            }
            val server = currentRepository.getServerDetails(serverId)
            _uiState.value = UiState.Success(server)
        } catch (e: Exception) {
            if (_uiState.value !is UiState.Success) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        } finally {
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (repository == null) {
                val settingsRepo = AppSettingsRepository(application)
                val settings = settingsRepo.settings.first()
                val baseUrl = settings.serverUrl.trimEnd('/')
                val httpClient = HttpClientFactory.create(baseUrl, settings.apiKey)
                val apiService = KernvoxApiService(httpClient)
                repository = ApiServersRepository(apiService)
            }
            loadDetails(showLoading = _uiState.value !is UiState.Success)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
