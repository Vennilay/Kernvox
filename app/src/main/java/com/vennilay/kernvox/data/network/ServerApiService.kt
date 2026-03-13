package com.vennilay.kernvox.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ServerApiService(
    private val httpClient: HttpClient,
) {
    suspend fun getServerStatus(
        host: String,
        port: Int,
    ): ServerStatusDto {
        return httpClient
            .get("http://$host:$port/status")
            .body()
    }
}
