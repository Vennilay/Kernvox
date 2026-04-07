package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.toServer
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.network.dto.MetricEntryDto
import com.vennilay.kernvox.data.network.dto.ProcessInfoDto
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import java.net.ConnectException
import java.net.UnknownHostException

class ApiServersRepository(
    private val apiService: KernvoxApiService,
) : ServersRepository {

    override suspend fun getServers(): List<Server> {
        return try {
            val dashboard = apiService.getDashboard()
            dashboard.servers.map { it.toServer() }
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override suspend fun getServerDetails(serverId: Int): Server {
        return try {
            apiService.getServerDetails(serverId).toServer()
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override suspend fun getServerProcesses(
        serverId: Int,
        limit: Int,
    ): List<ProcessInfoDto> {
        return try {
            val response = apiService.getServerProcesses(serverId, limit)
            response.processes
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override suspend fun getMetricsHistory(
        serverId: Int,
        from: String?,
        to: String?,
        limit: Int,
    ): List<MetricEntryDto> {
        return try {
            val response = apiService.getMetricsHistory(serverId, from, to, limit)
            response.metrics
        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    /**
     * Маппинг сетевых исключений в понятные сообщения об ошибках.
     */
    private fun mapException(e: Exception): Exception {
        return when (e) {
            is ConnectTimeoutException,
            is ConnectException,
            -> ApiException("Таймаут подключения. Проверьте URL сервера.", 0)

            is SocketTimeoutException ->
                ApiException("Превышено время ожидания ответа от сервера.", 0)

            is UnknownHostException ->
                ApiException("Не удалось найти сервер. Проверьте URL.", 0)

            is io.ktor.client.plugins.ClientRequestException -> {
                val status = e.response.status
                when (status) {
                    HttpStatusCode.Unauthorized ->
                        ApiException("Неверный API-ключ. Проверьте настройки.", 401)

                    HttpStatusCode.Forbidden ->
                        ApiException("Доступ запрещён. Проверьте API-ключ.", 403)

                    HttpStatusCode.NotFound ->
                        ApiException("Сервер не найден.", 404)

                    else ->
                        ApiException("Ошибка сервера: ${status.value} ${status.description}", status.value)
                }
            }

            is io.ktor.client.plugins.ServerResponseException ->
                ApiException("Ошибка на стороне сервера KernvoxHub.", 0)

            is kotlinx.serialization.SerializationException ->
                ApiException("Неожиданный формат ответа от сервера.", 0)

            else -> ApiException(e.message ?: "Неизвестная ошибка", 0)
        }
    }
}

/**
 * Исключение API с кодом ошибки.
 */
class ApiException(
    message: String,
    val code: Int,
) : Exception(message)
