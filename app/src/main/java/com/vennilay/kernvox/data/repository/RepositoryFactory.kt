package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.network.HttpClientFactory
import com.vennilay.kernvox.data.network.KernvoxApiService
import com.vennilay.kernvox.data.network.ServerUrlValidator
import com.vennilay.kernvox.data.storage.AppSettings

object RepositoryFactory {
    fun create(settings: AppSettings): ApiServersRepository {
        val baseUrl = settings.serverUrl.trimEnd('/')
        ServerUrlValidator.validate(baseUrl)
        val client = HttpClientFactory.create(baseUrl, settings.apiKey)
        return ApiServersRepository(
            apiService = KernvoxApiService(client, settings.actionKey),
            httpClient = client,
        )
    }
}
