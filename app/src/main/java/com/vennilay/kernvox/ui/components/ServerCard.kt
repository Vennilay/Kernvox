package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.theme.GreenSuccess
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.ui.utils.metricColor

@Composable
fun ServerCard(
    server: Server,
    modifier: Modifier = Modifier,
    onClick: ((Server) -> Unit)? = null,
) {
    val isOnline = server.isAvailable ?: false
    val cpuFormatted = remember(server.cpuPercent) {
        server.cpuPercent?.let { "%.1f%%".format(it) } ?: "—"
    }
    val ramFormatted = remember(server.ramPercent) {
        server.ramPercent?.let { "%.1f%%".format(it) } ?: "—"
    }
    val diskFormatted = remember(server.diskUsedPercent) {
        server.diskUsedPercent?.let { "%.1f%%".format(it) } ?: "—"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { onClick?.invoke(server) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isOnline) 1f else 0.55f)
                .padding(horizontal = Spacing.md, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOnline) GreenSuccess else MaterialTheme.colorScheme.error,
                        ),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                if (!isOnline) {
                    Text(
                        text = stringResource(R.string.servers_status_offline),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isOnline) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    InlineMetric(
                        label = stringResource(R.string.server_card_cpu_label),
                        value = cpuFormatted,
                        rawValue = server.cpuPercent,
                    )
                    InlineMetric(
                        label = stringResource(R.string.server_card_ram_label),
                        value = ramFormatted,
                        rawValue = server.ramPercent,
                    )
                    InlineMetric(
                        label = stringResource(R.string.server_card_disk_label),
                        value = diskFormatted,
                        rawValue = server.diskUsedPercent,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineMetric(
    label: String,
    value: String,
    rawValue: Float?,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp),
            fontWeight = FontWeight.SemiBold,
            color = metricColor(rawValue),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
