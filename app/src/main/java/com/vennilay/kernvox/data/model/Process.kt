package com.vennilay.kernvox.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Process(
    val pid: Int,
    val user: String,
    val cpuPercent: Float,
    val memoryPercent: Float,
    val command: String,
)
