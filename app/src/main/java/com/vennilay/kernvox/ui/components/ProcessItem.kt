package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.data.model.Process
import com.vennilay.kernvox.ui.theme.Spacing

@Composable
fun ProcessItem(
    process: Process,
    modifier: Modifier = Modifier,
) {
    val cpuFormatted = remember(process.cpuPercent) { "%.1f%%".format(process.cpuPercent) }
    val memoryFormatted = remember(process.memoryPercent) { "%.1f%%".format(process.memoryPercent) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // PID + user
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.width(72.dp),
        ) {
            Text(
                text = process.pid.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = process.user,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // command
        Text(
            text = process.command,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.sm),
        )

        // CPU chip
        MetricChip(
            label = "CPU",
            value = cpuFormatted,
            rawValue = process.cpuPercent,
            modifier = Modifier.wrapContentWidth(),
        )
        Spacer(Modifier.width(Spacing.md))
        // RAM chip
        MetricChip(
            label = "RAM",
            value = memoryFormatted,
            rawValue = process.memoryPercent,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}
