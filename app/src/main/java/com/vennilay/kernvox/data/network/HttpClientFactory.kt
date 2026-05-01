package com.vennilay.kernvox.data.network

import com.vennilay.kernvox.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(
        baseUrl: String = "",
        apiKey: String = "",
    ): HttpClient {
        return HttpClient(OkHttp) {
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            if (baseUrl.isNotBlank()) {
                defaultRequest {
                    url(baseUrl)
                    contentType(ContentType.Application.Json)
                    if (apiKey.isNotBlank()) {
                        header("X-API-Key", apiKey)
                    }
                }
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }
}