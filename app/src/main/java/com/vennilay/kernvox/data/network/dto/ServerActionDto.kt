package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerActionResponseDto(
    val id: Int,
    @SerialName("server_id") val serverId: Int,
    @SerialName("server_name") val serverName: String,
    val action: String,
    val status: String,
    val message: String? = null,
    @SerialName("created_at") val createdAt: String,
)
