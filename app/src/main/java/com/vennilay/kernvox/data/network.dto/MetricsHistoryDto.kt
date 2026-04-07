package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.Serializable

/**
 * DTO для элемента метрики в истории.
 */
@Serializable
data class MetricEntryDto(
    val id: Int,
    val cpu_percent: Float?,
    val ram_used_mb: Float?,
    val ram_total_mb: Float?,
    val ram_percent: Float?,
    val disk_used_percent: Float?,
    val network_rx_bytes: Float?,
    val network_tx_bytes: Float?,
    val uptime_seconds: Float?,
    val is_available: Boolean,
    val timestamp: String,
)

/**
 * DTO для ответа /api/v1/android/servers/{id}/metrics/history.
 */
@Serializable
data class MetricsHistoryResponseDto(
    val server_id: Int,
    val server_name: String,
    val count: Int,
    val metrics: List<MetricEntryDto>,
)
