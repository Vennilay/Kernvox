package com.vennilay.kernvox.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

/**
 * Форматирует время аптайма из секунд в читаемый формат.
 */
fun formatUptime(
    seconds: Long,
    daysUnit: String,
    hoursUnit: String,
    minutesUnit: String,
    secondsUnit: String,
): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60

    return buildString {
        if (days > 0) append("$days$daysUnit ")
        if (hours > 0 || days > 0) append("$hours$hoursUnit ")
        if (minutes > 0 || hours > 0 || days > 0) append("$minutes$minutesUnit")
        if (seconds < 60) append("${seconds % 60}$secondsUnit")
    }.trim()
}

/**
 * Форматирует байты в читаемый формат (КБ, МБ, ГБ).
 */
fun formatBytes(
    bytes: Float?,
    kbUnit: String,
    mbUnit: String,
    gbUnit: String,
    bytesUnit: String,
    noData: String,
): String {
    if (bytes == null) return noData
    return when {
        bytes >= 1_073_741_824f -> "${"%.1f".format(bytes / 1_073_741_824f)} $gbUnit"
        bytes >= 1_048_576f -> "${"%.1f".format(bytes / 1_048_576f)} $mbUnit"
        bytes >= 1024f -> "${"%.0f".format(bytes / 1024f)} $kbUnit"
        else -> "${"%.0f".format(bytes)} $bytesUnit"
    }
}

/**
 * Форматирует ISO-8601 timestamp в читаемый формат.
 * Пример входа: "2026-04-07T18:47:11.844222Z"
 * Пример выхода: "07.04.2026 21:47" (локальное время)
 */
fun formatTimestamp(isoTimestamp: String): String {
    return try {
        val instant = Instant.parse(isoTimestamp)
        outputFormatter.format(instant)
    } catch (e: Exception) {
        isoTimestamp
    }
}
