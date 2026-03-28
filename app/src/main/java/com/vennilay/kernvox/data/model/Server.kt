package com.vennilay.kernvox.data.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * Модель данных сервера.
 * Аннотация @Stable помогает Compose оптимизировать перерисовки.
 */
@Stable
@Serializable
data class Server(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val uptimeSeconds: Long,
    val isAvailable: Boolean,
    val lastCheckedAtEpochMillis: Long,
)
