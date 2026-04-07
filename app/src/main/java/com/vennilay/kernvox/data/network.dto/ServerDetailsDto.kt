package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.Serializable

/**
 * DTO для подробной информации о сервере.
 * Ответ /api/v1/android/servers/{id}/details.
 */
@Serializable
data class ServerDetailsDto(
    val id: Int,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String?,
    val cpu_cores: Int?,
    val uptime_seconds: Float?,
    val uptime_formatted: String?,
    val network_rx_bytes: Float?,
    val network_tx_bytes: Float?,
    val cpu_percent: Float?,
    val ram_used_mb: Float?,
    val ram_total_mb: Float?,
    val ram_percent: Float?,
    val disk_used_percent: Float?,
    val is_available: Boolean?,
    val last_metric_timestamp: String?,
)

/**
 * DTO для информации о процессе.
 */
@Serializable
data class ProcessInfoDto(
    val pid: Int,
    val user: String,
    val cpu_percent: Float,
    val memory_percent: Float,
    val command: String,
)

/**
 * DTO для ответа /api/v1/android/servers/{id}/processes.
 */
@Serializable
data class ServerProcessesDto(
    val server_id: Int,
    val server_name: String,
    val processes: List<ProcessInfoDto>,
    val total_processes: Int,
    val timestamp: String,
)
