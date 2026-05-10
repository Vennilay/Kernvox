package com.vennilay.kernvox.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ServerActionResult(
    val action: String,
    val status: String,
    val message: String?,
    val createdAt: String,
)
