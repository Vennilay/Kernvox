package com.vennilay.kernvox.data.model

data class MetricEntry(
    val id: Int,
    val cpuPercent: Float?,
    val ramUsedMb: Float?,
    val ramTotalMb: Float?,
    val ramPercent: Float?,
    val diskUsedPercent: Float?,
    val networkRxBytes: Float?,
    val networkTxBytes: Float?,
    val uptimeSeconds: Float?,
    val isAvailable: Boolean,
    val timestamp: String,
)
