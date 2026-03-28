package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Компонент отображения статуса сервера в виде бейджа с точкой-индикатором.
 *
 * @param isOnline Статус сервера: true — онлайн, false — оффлайн
 * @param modifier Модификатор для компонента
 */
@Composable
fun StatusBadge(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.run {
        if (isOnline) primaryContainer else errorContainer
    }

    val textColor = MaterialTheme.colorScheme.run {
        if (isOnline) onPrimaryContainer else onErrorContainer
    }

    val indicatorColor = MaterialTheme.colorScheme.run {
        if (isOnline) primary else error
    }

    val statusText = if (isOnline) "Онлайн" else "Оффлайн"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Точка-индикатор статуса
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
