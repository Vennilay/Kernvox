package com.vennilay.kernvox.data.network

import com.vennilay.kernvox.data.network.dto.DashboardResponseDto
import com.vennilay.kernvox.data.network.dto.MetricsHistoryResponseDto
import com.vennilay.kernvox.data.network.dto.ServerDetailsDto
import com.vennilay.kernvox.data.network.dto.ServerProcessesDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KernvoxApiService(
    private val httpClient: HttpClient,
) {

    /**
     * GET /api/v1/android/dashboard
     * Сводка по всем серверам.
     */
    suspend fun getDashboard(): DashboardResponseDto {
        return httpClient.get("/api/v1/android/dashboard").body()
    }

    /**
     * GET /api/v1/android/servers/{id}/details
     * Полная информация о сервере.
     */
    suspend fun getServerDetails(serverId: Int): ServerDetailsDto {
        return httpClient.get("/api/v1/android/servers/$serverId/details").body()
    }

    /**
     * GET /api/v1/android/servers/{id}/processes?limit=50
     * Запущенные процессы на сервере (через SSH).
     */
    suspend fun getServerProcesses(
        serverId: Int,
        limit: Int = 50,
    ): ServerProcessesDto {
        return httpClient
            .get("/api/v1/android/servers/$serverId/processes") {
                parameter("limit", limit)
            }
            .body()
    }

    /**
     * GET /api/v1/android/servers/{id}/metrics/history?from=&to=&limit=100
     * История метрик сервера.
     */
    suspend fun getMetricsHistory(
        serverId: Int,
        from: String? = null,
        to: String? = null,
        limit: Int = 100,
    ): MetricsHistoryResponseDto {
        return httpClient
            .get("/api/v1/android/servers/$serverId/metrics/history") {
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
                parameter("limit", limit)
            }
            .body()
    }
}
