package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.Server

interface ServersRepository {
    suspend fun getServers(): List<Server>

    suspend fun addServer(server: Server)
}
