package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.MetricEntry
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.ui.utils.formatTimestamp
import com.vennilay.kernvox.ui.utils.metricColor

@Composable
fun MetricHistoryRow(
    entry: MetricEntry,
    modifier: Modifier = Modifier,
) {
    val timestamp = remember(entry.timestamp) { formatTimestamp(entry.timestamp) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!entry.isAvailable) {
                    Text(
                        text = stringResource(R.string.history_unavailable),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (entry.isAvailable) {
                Spacer(Modifier.height(Spacing.xs))
                MetricBar(
                    label = stringResource(R.string.history_cpu_label),
                    value = entry.cpuPercent,
                )
                MetricBar(
                    label = stringResource(R.string.history_ram_label),
                    value = entry.ramPercent,
                )
                MetricBar(
                    label = stringResource(R.string.history_disk_label),
                    value = entry.diskUsedPercent,
                )
            }
        }
    }
}

@Composable
private fun MetricBar(label: String, value: Float?) {
    if (value == null) return
    val color = metricColor(value)
    val formattedValue = remember(value) { "%.0f%%".format(value) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = formattedValue,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End,
        )
    }
}
