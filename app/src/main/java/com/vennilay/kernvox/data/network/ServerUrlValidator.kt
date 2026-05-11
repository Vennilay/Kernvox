package com.vennilay.kernvox.data.network

import com.vennilay.kernvox.BuildConfig
import java.net.URI
import java.util.Locale

class InsecureServerUrlException : IllegalArgumentException(
    "HTTPS is required for non-local server URLs in release builds.",
)

object ServerUrlValidator {
    fun isAllowed(
        serverUrl: String,
        isDebugBuild: Boolean = BuildConfig.DEBUG,
    ): Boolean {
        val uri = runCatching { URI(serverUrl.trim()) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false
        return when (scheme) {
            HTTPS_SCHEME -> true
            HTTP_SCHEME -> isDebugBuild || uri.host.isLocalDevelopmentHost()
            else -> false
        }
    }

    fun validate(
        serverUrl: String,
        isDebugBuild: Boolean = BuildConfig.DEBUG,
    ) {
        if (!isAllowed(serverUrl, isDebugBuild)) {
            throw InsecureServerUrlException()
        }
    }

    private fun String?.isLocalDevelopmentHost(): Boolean {
        val host = this?.lowercase(Locale.ROOT) ?: return false
        return host == "localhost" ||
            host == "127.0.0.1" ||
            host == "::1" ||
            host == "10.0.2.2"
    }

    private const val HTTP_SCHEME = "http"
    private const val HTTPS_SCHEME = "https"
}
