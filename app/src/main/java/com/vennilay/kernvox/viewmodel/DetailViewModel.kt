package com.vennilay.kernvox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.repository.ApiServersRepository
import com.vennilay.kernvox.data.repository.RepositoryFactory
import com.vennilay.kernvox.data.repository.toUserFriendlyMessageRes
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.state.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _isRebooting = MutableStateFlow(false)
    val isRebooting: StateFlow<Boolean> = _isRebooting.asStateFlow()

    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val messages: SharedFlow<UiText> = _messages.asSharedFlow()

    private var pollingJob: Job? = null
    private var processesJob: Job? = null
    private var historyJob: Job? = null
    private var repository: ApiServersRepository? = null
    private var selectedPage: Int = PAGE_OVERVIEW
    private var processesSkippedForOffline = false

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            ensureRepository()

            loadDetails(showLoading = true)

            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30_000)
                    loadDetails(showLoading = false, showRefreshing = false)
                }
            }
        }
    }

    private suspend fun loadDetails(
        showLoading: Boolean,
        showRefreshing: Boolean = true,
    ) {
        val currentRepository = ensureRepository() ?: return
        try {
            if (showLoading && _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            } else if (showRefreshing) {
                _isRefreshing.value = true
            }
            val server = withContext(Dispatchers.IO) {
                currentRepository.getServerDetails(serverId)
            }
            _uiState.value = UiState.Success(server)
            if (server.isAvailable == false && selectedPage == PAGE_PROCESSES) {
                _totalProcesses.value = 0
                _processesState.value = UiState.Success(emptyList())
                processesSkippedForOffline = true
            } else if (server.isAvailable == true && selectedPage == PAGE_PROCESSES) {
                loadProcesses(force = processesSkippedForOffline || _processesState.value !is UiState.Success)
            }
        } catch (e: Exception) {
            if (_uiState.value !is UiState.Success) {
                _uiState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes()))
            }
        } finally {
            if (showRefreshing) {
                _isRefreshing.value = false
            }
        }
    }

    fun onTabSelected(page: Int) {
        selectedPage = page
        when (page) {
            PAGE_PROCESSES -> loadProcesses()
            PAGE_HISTORY -> loadHistory()
        }
    }

    /**
     * Загружает данные о процессах только тогда, когда это необходимо вкладке «Процессы».
     *
     * Офлайн-серверы пропускают вызов SSH/API и переводят вкладку в пустое офлайн-состояние вместо того, чтобы
     * оставлять индикатор загрузки активным. Когда при последующем опросе сервер будет отмечен как онлайн, выбранная вкладка сможет
     * снова запрашивать данные о процессах в обычном режиме.
     */
    fun loadProcesses(force: Boolean = false) {
        if (processesJob?.isActive == true) return
        processesJob = viewModelScope.launch {
            val currentRepository = ensureRepository() ?: return@launch
            try {
                val server = (_uiState.value as? UiState.Success)?.data
                if (server?.isAvailable == false) {
                    _totalProcesses.value = 0
                    _processesState.value = UiState.Success(emptyList())
                    processesSkippedForOffline = true
                    return@launch
                }
                if (_processesState.value !is UiState.Success) {
                    _processesState.value = UiState.Loading
                } else if (!force) {
                    return@launch
                }
                val processes = withContext(Dispatchers.IO) {
                    currentRepository.getServerProcesses(serverId)
                }
                _totalProcesses.value = processes.size
                _processesState.value = UiState.Success(processes)
                processesSkippedForOffline = false
            } catch (e: Exception) {
                if (_processesState.value !is UiState.Success) {
                    _processesState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes(com.vennilay.kernvox.R.string.error_processes_load)))
                } else {
                    _messages.emit(UiText.resource(e.toUserFriendlyMessageRes(com.vennilay.kernvox.R.string.error_processes_update)))
                }
            }
        }
    }

    fun loadHistory(force: Boolean = false) {
        if (historyJob?.isActive == true) return
        historyJob = viewModelScope.launch {
            val currentRepository = ensureRepository() ?: return@launch
            try {
                if (_historyState.value !is UiState.Success) {
                    _historyState.value = UiState.Loading
                } else if (!force) {
                    return@launch
                }
                val history = withContext(Dispatchers.IO) {
                    currentRepository.getMetricsHistory(serverId, limit = 50)
                }
                _historyState.value = UiState.Success(history)
            } catch (e: Exception) {
                if (_historyState.value !is UiState.Success) {
                    _historyState.value = UiState.Error(UiText.resource(e.toUserFriendlyMessageRes(com.vennilay.kernvox.R.string.error_history_load)))
                } else {
                    _messages.emit(UiText.resource(e.toUserFriendlyMessageRes(com.vennilay.kernvox.R.string.error_history_update)))
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadDetails(showLoading = _uiState.value !is UiState.Success)
            when (selectedPage) {
                PAGE_PROCESSES -> loadProcesses(force = true)
                PAGE_HISTORY -> loadHistory(force = true)
            }
        }
    }

    fun rebootServer() {
        viewModelScope.launch {
            val currentRepository = repository ?: run {
                _messages.emit(UiText.resource(com.vennilay.kernvox.R.string.error_settings_not_loaded))
                return@launch
            }

            if (_isRebooting.value) {
                return@launch
            }

            try {
                _isRebooting.value = true
                val actionResult = currentRepository.rebootServer(serverId)
                _messages.emit(
                    UiText.resource(com.vennilay.kernvox.R.string.server_detail_reboot_success)
                )
            } catch (e: Exception) {
                _messages.emit(UiText.resource(e.toUserFriendlyMessageRes(com.vennilay.kernvox.R.string.error_reboot_send)))
            } finally {
                _isRebooting.value = false
            }
        }
    }

    private suspend fun ensureRepository(): ApiServersRepository? {
        repository?.let { return it }
        val settings = AppSettingsRepository(application).settings.first()
        repository = RepositoryFactory.create(settings)
        return repository
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        processesJob?.cancel()
        historyJob?.cancel()
        repository?.close()
    }

    private companion object {
        const val PAGE_OVERVIEW = 0
        const val PAGE_PROCESSES = 1
        const val PAGE_HISTORY = 2
    }
}
