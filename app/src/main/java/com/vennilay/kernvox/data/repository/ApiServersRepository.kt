package com.vennilay.kernvox.data.repository

import android.util.Log
import androidx.annotation.StringRes
import com.vennilay.kernvox.R
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
            Log.w(TAG, "KernvoxHub client error: ${e.response.status}", e)
            throw mapClientException(e)
        } catch (e: ServerResponseException) {
            Log.w(TAG, "KernvoxHub server error: ${e.response.status}", e)
            throw mapServerException(e)
        } catch (e: Exception) {
            Log.w(TAG, "KernvoxHub request failed", e)
            throw mapException(e)
        }

    private fun mapException(e: Exception): Exception {
        return when (e) {
            is ConnectTimeoutException,
            is ConnectException,
                -> ApiException(R.string.error_connection_failed, 0)

            is SocketTimeoutException ->
                ApiException(R.string.error_response_timeout, 0)

            is UnknownHostException ->
                ApiException(R.string.error_connection_failed, 0)

            is kotlinx.serialization.SerializationException ->
                ApiException(R.string.error_response_format, 0)

            else -> ApiException(userFriendlyApiMessageRes(e.message), 0)
        }
    }

    private suspend fun mapClientException(e: ClientRequestException): ApiException {
        val detail = extractErrorDetail(e.response.bodyAsText())
        return when (e.response.status) {
            HttpStatusCode.Unauthorized ->
                ApiException(R.string.error_invalid_api_key, 401)

            HttpStatusCode.Forbidden ->
                ApiException(
                    userFriendlyApiMessageRes(detail ?: "Forbidden"),
                    403,
                )

            HttpStatusCode.NotFound ->
                ApiException(userFriendlyApiMessageRes(detail ?: "Server not found"), 404)

            HttpStatusCode.TooManyRequests ->
                ApiException(userFriendlyApiMessageRes(detail ?: "Too many requests"), 429)

            else ->
                ApiException(
                    userFriendlyApiMessageRes(detail ?: e.response.status.description),
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
                            R.string.error_action_unavailable

                        "Host key verification failed" ->
                            R.string.error_host_verification_failed

                        else -> userFriendlyApiMessageRes(detail ?: "Service unavailable")
                    },
                    e.response.status.value,
                )

            else ->
                ApiException(
                    userFriendlyApiMessageRes(detail ?: "KernvoxHub server error"),
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
    @param:StringRes val messageResId: Int,
    val code: Int,
) : Exception("api_error_$messageResId")

private const val TAG = "ApiServersRepository"
