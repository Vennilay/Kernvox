package com.vennilay.kernvox.data.model

import kotlinx.serialization.Serializable

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
