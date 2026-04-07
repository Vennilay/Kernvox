package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.network.dto.MetricEntryDto
import com.vennilay.kernvox.data.network.dto.ProcessInfoDto

interface ServersRepository {
    /**
     * Получить список серверов из dashboard.
     */
    suspend fun getServers(): List<Server>

    /**
     * Получить детальную информацию о сервере.
     */
    suspend fun getServerDetails(serverId: Int): Server

    /**
     * Получить список процессов на сервере.
     */
    suspend fun getServerProcesses(serverId: Int, limit: Int = 50): List<ProcessInfoDto>

    /**
     * Получить историю метрик сервера.
     */
    suspend fun getMetricsHistory(
        serverId: Int,
        from: String? = null,
        to: String? = null,
        limit: Int = 100,
    ): List<MetricEntryDto>
}
