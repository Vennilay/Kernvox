package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.RepositoryFactory
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
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<Server>>(UiState.Loading)
    val uiState: StateFlow<UiState<Server>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _processesState = MutableStateFlow<UiState<List<Process>>>(UiState.Loading)
    val processesState: StateFlow<UiState<List<Process>>> = _processesState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<List<MetricEntry>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<MetricEntry>>> = _historyState.asStateFlow()

    private val _totalProcesses = MutableStateFlow(0)
    val totalProcesses: StateFlow<Int> = _totalProcesses.asStateFlow()

    private var pollingJob: Job? = null
    private var repository: ApiServersRepository? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            val settings = AppSettingsRepository(application).settings.first()
            repository = RepositoryFactory.create(settings)

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

        viewModelScope.launch {
            try {
                val processes = currentRepository.getServerProcesses(serverId)
                _totalProcesses.value = processes.size
                _processesState.value = UiState.Success(processes)
            } catch (e: Exception) {
                _processesState.value = UiState.Error(e.message ?: "Ошибка загрузки процессов")
            }
        }

        viewModelScope.launch {
            try {
                val history = currentRepository.getMetricsHistory(serverId, limit = 50)
                _historyState.value = UiState.Success(history)
            } catch (e: Exception) {
                _historyState.value = UiState.Error(e.message ?: "Ошибка загрузки истории")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (repository == null) {
                val settings = AppSettingsRepository(application).settings.first()
                repository = RepositoryFactory.create(settings)
            }
            loadDetails(showLoading = _uiState.value !is UiState.Success)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        repository?.close()
    }
}
