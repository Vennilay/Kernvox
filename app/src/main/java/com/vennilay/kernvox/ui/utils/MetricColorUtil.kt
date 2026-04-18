package com.vennilay.kernvox.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vennilay.kernvox.ui.theme.AmberWarning
import com.vennilay.kernvox.ui.theme.GreenSuccess

@Composable
fun metricColor(value: Float?): Color = when {
    value == null -> MaterialTheme.colorScheme.onSurfaceVariant
    value < 60f -> GreenSuccess
    value < 80f -> AmberWarning
    else -> MaterialTheme.colorScheme.error
}
