package com.vennilay.kernvox.data.network.dto

import kotlinx.serialization.Serializable

/**
 * DTO для элемента сервера в dashboard.
 */
@Serializable
data class DashboardServerDto(
    val id: Int,
    val name: String,
    val host: String,
    val is_active: Boolean,
    val is_available: Boolean?,
    val cpu_percent: Float?,
    val ram_percent: Float?,
    val disk_used_percent: Float?,
    val last_update: String?,
)

/**
 * DTO для ответа /api/v1/android/dashboard.
 */
@Serializable
data class DashboardResponseDto(
    val total_servers: Int,
    val active_servers: Int,
    val available_servers: Int,
    val servers: List<DashboardServerDto>,
    val timestamp: String,
)
