package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Универсальный компонент для отображения иконки в круглом контейнере.
 *
 * @param icon Ресурс иконки для отображения
 * @param modifier Модификатор для компонента
 * @param containerColor Цвет фона контейнера
 * @param iconColor Цвет иконки
 * @param size Размер иконки (по умолчанию 40.dp для контейнера, 20.dp для иконки)
 */
@Composable
fun IconCircle(
    icon: Int,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerSize: Int = 40,
    iconSize: Int = 20
) {
    Box(
        modifier = modifier
            .size(containerSize.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize.dp),
            tint = iconColor
        )
    }
}

/**
 * Универсальный компонент для отображения иконки в круглом контейнере с ImageVector.
 *
 * @param icon ImageVector иконки для отображения
 * @param modifier Модификатор для компонента
 * @param containerColor Цвет фона контейнера
 * @param iconColor Цвет иконки
 */
@Composable
fun IconCircle(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerSize: Int = 40,
    iconSize: Int = 20
) {
    Box(
        modifier = modifier
            .size(containerSize.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize.dp),
            tint = iconColor
        )
    }
}
