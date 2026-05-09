package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.data.model.ServerActionResult
import com.vennilay.kernvox.data.model.toMetricEntry
import com.vennilay.kernvox.data.model.toProcess
import com.vennilay.kernvox.data.model.toServer
import com.vennilay.kernvox.data.model.toServerActionResult
import com.vennilay.kernvox.data.network.KernvoxApiService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.net.ConnectException
import java.net.UnknownHostException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    override suspend fun rebootServer(serverId: Int): ServerActionResult =
        safeCall { apiService.rebootServer(serverId).toServerActionResult() }

    fun close() = httpClient.close()

    private suspend fun <T> safeCall(block: suspend () -> T): T =
        try {
            block()
        } catch (e: ClientRequestException) {
            throw mapClientException(e)
        } catch (e: ServerResponseException) {
            throw mapServerException(e)
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

            is kotlinx.serialization.SerializationException ->
                ApiException("Неожиданный формат ответа от сервера.", 0)

            else -> ApiException(e.message ?: "Неизвестная ошибка", 0)
        }
    }

    private suspend fun mapClientException(e: ClientRequestException): ApiException {
        val detail = extractErrorDetail(e.response.bodyAsText())
        return when (e.response.status) {
            HttpStatusCode.Unauthorized ->
                ApiException("Неверный API-ключ. Проверьте настройки.", 401)

            HttpStatusCode.Forbidden ->
                ApiException(
                    when (detail) {
                        "Server action key is required" ->
                            "Для перезагрузки нужен X-Action-Key. Укажите его в настройках."

                        else -> detail ?: "Доступ запрещён. Проверьте ключи доступа."
                    },
                    403,
                )

            HttpStatusCode.NotFound ->
                ApiException(detail ?: "Сервер не найден.", 404)

            HttpStatusCode.TooManyRequests ->
                ApiException(detail ?: "Слишком много запросов. Повторите позже.", 429)

            else ->
                ApiException(
                    detail ?: "Ошибка сервера: ${e.response.status.value} ${e.response.status.description}",
                    e.response.status.value,
                )
        }
    }

    private suspend fun mapServerException(e: ServerResponseException): ApiException {
        val detail = extractErrorDetail(e.response.bodyAsText())
        return when (e.response.status) {
            HttpStatusCode.ServiceUnavailable ->
                ApiException(
                    when (detail) {
                        "SERVER_ACTION_TOKEN is not configured" ->
                            "На стороне KernvoxHub не настроен SERVER_ACTION_TOKEN."

                        "Host key verification failed" ->
                            "Проверка SSH host key не пройдена. Нужна проверка сервера в KernvoxHub."

                        else -> detail ?: "Сервис временно недоступен."
                    },
                    e.response.status.value,
                )

            else ->
                ApiException(
                    detail ?: "Ошибка на стороне сервера KernvoxHub.",
                    e.response.status.value,
                )
        }
    }

    private fun extractErrorDetail(body: String): String? {
        return runCatching {
            Json.parseToJsonElement(body)
                .jsonObject["detail"]
                ?.jsonPrimitive
                ?.content
                ?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: body.trim().takeIf { it.isNotBlank() }
    }
}

class ApiException(
    message: String,
    val code: Int,
) : Exception(message)
