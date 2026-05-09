package com.vennilay.kernvox.data.model

data class ServerActionResult(
    val action: String,
    val status: String,
    val message: String?,
    val createdAt: String,
)
