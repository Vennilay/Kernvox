package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerDetailsDto(
    val id: Int,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String?,
    @SerialName("cpu_cores") val cpuCores: Int?,
    @SerialName("uptime_seconds") val uptimeSeconds: Float?,
    @SerialName("uptime_formatted") val uptimeFormatted: String?,
    @SerialName("network_rx_bytes") val networkRxBytes: Float?,
    @SerialName("network_tx_bytes") val networkTxBytes: Float?,
    @SerialName("cpu_percent") val cpuPercent: Float?,
    @SerialName("ram_used_mb") val ramUsedMb: Float?,
    @SerialName("ram_total_mb") val ramTotalMb: Float?,
    @SerialName("ram_percent") val ramPercent: Float?,
    @SerialName("disk_used_percent") val diskUsedPercent: Float?,
    @SerialName("is_available") val isAvailable: Boolean?,
    @SerialName("last_metric_timestamp") val lastMetricTimestamp: String?,
)

@Serializable
data class ProcessInfoDto(
    val pid: Int,
    val user: String,
    @SerialName("cpu_percent") val cpuPercent: Float,
    @SerialName("memory_percent") val memoryPercent: Float,
    val command: String,
)

@Serializable
data class ServerProcessesDto(
    @SerialName("server_id") val serverId: Int,
    @SerialName("server_name") val serverName: String,
    val processes: List<ProcessInfoDto>,
    @SerialName("total_processes") val totalProcesses: Int,
    val timestamp: String,
)
