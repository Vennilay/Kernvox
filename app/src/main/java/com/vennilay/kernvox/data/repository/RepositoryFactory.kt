package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.network.HttpClientFactory
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.storage.AppSettings

object RepositoryFactory {
    fun create(settings: AppSettings): ApiServersRepository {
        val client = HttpClientFactory.create(settings.serverUrl.trimEnd('/'), settings.apiKey)
        return ApiServersRepository(KernvoxApiService(client), client)
    }
}
