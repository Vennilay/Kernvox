package com.vennilay.kernvox.data.model

import androidx.compose.runtime.Stable

/**
 * Domain-модель сервера для отображения в UI.
 * Объединяет данные из dashboard и detail эндпоинтов.
 */
@Stable
data class Server(
    val id: Int,
    val name: String,
    val host: String,
    val port: Int,
    val isActive: Boolean,
    val isAvailable: Boolean?,
    val cpuPercent: Float?,
    val ramPercent: Float?,
    val diskUsedPercent: Float?,
    val cpuCores: Int?,
    val uptimeSeconds: Float?,
    val uptimeFormatted: String?,
    val ramUsedMb: Float?,
    val ramTotalMb: Float?,
    val networkRxBytes: Float?,
    val networkTxBytes: Float?,
    val lastMetricTimestamp: String?,
    val username: String?,
)
