package com.vennilay.kernvox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.repository.FakeServersRepository
import com.vennilay.kernvox.data.repository.ServersRepository
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

    init {
        loadServers()
    }

    fun loadServers() {
        viewModelScope.launch {
            _servers.value = serversRepository.getServers()
        }
    }

    fun addServer(server: Server) {
        viewModelScope.launch {
            serversRepository.addServer(server)
            _servers.update { it + server }
        }
    }
}
