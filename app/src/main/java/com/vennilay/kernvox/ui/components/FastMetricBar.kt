package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.ui.utils.metricColor

@Composable
fun FastMetricBar(
    value: Float?,
    modifier: Modifier = Modifier,
) {
    val progress = ((value ?: 0f) / 100f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(5.dp)
                .background(metricColor(value)),
        )
    }
}

@Composable
fun StatusDot(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(8.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (isOnline) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            ),
    )
}
