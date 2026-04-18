package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.ui.utils.formatTimestamp
import com.vennilay.kernvox.ui.utils.formatUptime

@Composable
fun ServerCard(
    server: Server,
    modifier: Modifier = Modifier,
    onClick: ((Server) -> Unit)? = null,
) {
    val noData = stringResource(R.string.server_card_no_data)
    val daysUnit = stringResource(R.string.time_days)
    val hoursUnit = stringResource(R.string.time_hours)
    val minutesUnit = stringResource(R.string.time_minutes)
    val secondsUnit = stringResource(R.string.time_seconds)

    val uptimeFormatted =
        remember(server.uptimeSeconds, daysUnit, hoursUnit, minutesUnit, secondsUnit) {
            server.uptimeSeconds?.toLong()
                ?.let { formatUptime(it, daysUnit, hoursUnit, minutesUnit, secondsUnit) } ?: noData
        }
    val lastCheckedFormatted = remember(server.lastMetricTimestamp) {
        server.lastMetricTimestamp?.let { formatTimestamp(it) } ?: noData
    }
    val cpuFormatted = remember(server.cpuPercent) {
        server.cpuPercent?.let { "%.1f%%".format(it) } ?: noData
    }
    val ramFormatted = remember(server.ramPercent) {
        server.ramPercent?.let { "%.1f%%".format(it) } ?: noData
    }
    val diskFormatted = remember(server.diskUsedPercent) {
        server.diskUsedPercent?.let { "%.1f%%".format(it) } ?: noData
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { onClick?.invoke(server) },
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircle(
                        icon = R.drawable.ic_server_placeholder,
                        containerSize = 40,
                        iconSize = 20,
                        rounded = false,
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                StatusBadge(isOnline = server.isAvailable ?: false)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCircle(
                    icon = R.drawable.ic_location,
                    containerSize = 32,
                    iconSize = 16,
                    rounded = true,
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = "${server.host}:${server.port}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.server_card_uptime_label_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = uptimeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.server_card_last_check_label_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = lastCheckedFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip(
                    label = stringResource(R.string.server_card_cpu_label),
                    value = cpuFormatted,
                    rawValue = server.cpuPercent,
                )
                MetricChip(
                    label = stringResource(R.string.server_card_ram_label),
                    value = ramFormatted,
                    rawValue = server.ramPercent,
                )
                MetricChip(
                    label = stringResource(R.string.server_card_disk_label),
                    value = diskFormatted,
                    rawValue = server.diskUsedPercent,
                )
            }
        }
    }
}
