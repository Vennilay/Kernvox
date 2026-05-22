package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardServerDto(
    val id: Int,
    val name: String,
    val host: String,
    val port: Int? = null,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_available") val isAvailable: Boolean?,
    @SerialName("cpu_percent") val cpuPercent: Float?,
    @SerialName("ram_percent") val ramPercent: Float?,
    @SerialName("disk_used_percent") val diskUsedPercent: Float?,
    @SerialName("last_update") val lastUpdate: String?,
)

@Serializable
data class DashboardResponseDto(
    @SerialName("total_servers") val totalServers: Int,
    @SerialName("active_servers") val activeServers: Int,
    @SerialName("available_servers") val availableServers: Int,
    val servers: List<DashboardServerDto>,
    val timestamp: String,
)
