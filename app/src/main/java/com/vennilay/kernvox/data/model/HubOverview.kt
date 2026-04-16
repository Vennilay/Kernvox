package com.vennilay.kernvox.data.model

import androidx.compose.runtime.Stable

@Stable
data class HubOverview(
    val name: String,
    val baseUrl: String,
    val host: String,
    val port: Int?,
    val totalNodes: Int,
    val availableNodes: Int,
    val lastUpdate: String?,
)
