package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetricEntryDto(
    val id: Int,
    @SerialName("cpu_percent") val cpuPercent: Float?,
    @SerialName("ram_used_mb") val ramUsedMb: Float?,
    @SerialName("ram_total_mb") val ramTotalMb: Float?,
    @SerialName("ram_percent") val ramPercent: Float?,
    @SerialName("disk_used_percent") val diskUsedPercent: Float?,
    @SerialName("network_rx_bytes") val networkRxBytes: Float?,
    @SerialName("network_tx_bytes") val networkTxBytes: Float?,
    @SerialName("uptime_seconds") val uptimeSeconds: Float?,
    @SerialName("is_available") val isAvailable: Boolean,
    val timestamp: String,
)

@Serializable
data class MetricsHistoryResponseDto(
    @SerialName("server_id") val serverId: Int,
    @SerialName("server_name") val serverName: String,
    val count: Int,
    val metrics: List<MetricEntryDto>,
)
