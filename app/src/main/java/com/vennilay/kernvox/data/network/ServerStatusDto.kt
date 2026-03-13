package com.vennilay.kernvox.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatusDto(
    val uptimeSeconds: Long,
    val isAvailable: Boolean,
    val lastCheckedAtEpochMillis: Long,
)
