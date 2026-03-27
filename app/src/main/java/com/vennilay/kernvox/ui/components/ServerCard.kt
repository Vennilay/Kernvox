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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.utils.formatLastChecked
import com.vennilay.kernvox.ui.utils.formatUptime

/**
 * Карточка сервера с отображением всей ключевой информации.
 *
 * @param server Данные сервера для отображения
 * @param modifier Модификатор для компонента
 * @param onClick Обработчик нажатия на карточку (опционально), передаёт сервер
 */
@Composable
fun ServerCard(
    server: Server,
    modifier: Modifier = Modifier,
    onClick: ((Server) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { onClick?.invoke(server) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок карточки: иконка + название + статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Иконка сервера
                    IconCircle(
                        icon = R.drawable.ic_server_placeholder,
                        containerSize = 40,
                        iconSize = 20,
                        rounded = false
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Название сервера
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Бейдж статуса
                StatusBadge(isOnline = server.isAvailable)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Хост и порт
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconCircle(
                    icon = R.drawable.ic_location,
                    containerSize = 32,
                    iconSize = 16,
                    rounded = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${server.host}:${server.port}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Дополнительная информация: uptime и последняя проверка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Uptime
                Column {
                    Text(
                        text = "Аптайм",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatUptime(server.uptimeSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Последняя проверка
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Проверен",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatLastChecked(server.lastCheckedAtEpochMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
