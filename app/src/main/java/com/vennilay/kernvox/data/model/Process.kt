package com.vennilay.kernvox.data.model

data class Process(
    val pid: Int,
    val user: String,
    val cpuPercent: Float,
    val memoryPercent: Float,
    val command: String,
)
