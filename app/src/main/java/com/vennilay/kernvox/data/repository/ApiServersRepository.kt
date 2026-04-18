package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.toMetricEntry
import com.vennilay.kernvox.data.model.toProcess
import com.vennilay.kernvox.data.model.toServer
import com.vennilay.kernvox.data.network.KernvoxApiService
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import java.net.ConnectException
import java.net.UnknownHostException

class ApiServersRepository(
    private val apiService: KernvoxApiService,
    private val httpClient: HttpClient,
) : ServersRepository {

    override suspend fun getServers(): List<Server> =
        safeCall { apiService.getDashboard().servers.map { it.toServer() } }

    override suspend fun getServerDetails(serverId: Int): Server =
        safeCall { apiService.getServerDetails(serverId).toServer() }

    override suspend fun getServerProcesses(serverId: Int, limit: Int): List<Process> =
        safeCall { apiService.getServerProcesses(serverId, limit).processes.map { it.toProcess() } }

    override suspend fun getMetricsHistory(
        serverId: Int,
        from: String?,
        to: String?,
        limit: Int,
    ): List<MetricEntry> =
        safeCall {
            apiService.getMetricsHistory(
                serverId,
                from,
                to,
                limit
            ).metrics.map { it.toMetricEntry() }
        }

    fun close() = httpClient.close()

    private suspend fun <T> safeCall(block: suspend () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            throw mapException(e)
        }

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
                        ApiException(
                            "Ошибка сервера: ${status.value} ${status.description}",
                            status.value
                        )
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

class ApiException(
    message: String,
    val code: Int,
) : Exception(message)
