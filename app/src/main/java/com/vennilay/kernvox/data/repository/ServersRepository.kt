package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.ServerActionResult

interface ServersRepository {
    suspend fun getServers(): List<Server>

    suspend fun getServerDetails(serverId: Int): Server

    suspend fun getServerProcesses(serverId: Int, limit: Int = 50): List<Process>

    suspend fun getMetricsHistory(
        serverId: Int,
        from: String? = null,
        to: String? = null,
        limit: Int = 100,
    ): List<MetricEntry>

    suspend fun rebootServer(serverId: Int): ServerActionResult
}
