package com.vennilay.kernvox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.repository.FakeServersRepository
import com.vennilay.kernvox.data.repository.ServersRepository
import com.vennilay.kernvox.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServersViewModel(
    private val serversRepository: ServersRepository = FakeServersRepository(),
) : ViewModel() {

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Server>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Server>>> = _uiState.asStateFlow()

    init {
        loadServers()
    }

    fun loadServers() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                _servers.value = serversRepository.getServers()
                _uiState.value = UiState.Success(_servers.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun addServer(server: Server) {
        viewModelScope.launch {
            try {
                serversRepository.addServer(server)
                _servers.update { it + server }
                _uiState.value = UiState.Success(_servers.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
