package com.vennilay.kernvox.ui.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Форматирует время аптайма из секунд в читаемый формат.
 */
fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60

    return buildString {
        if (days > 0) append("${days}дн ")
        if (hours > 0 || days > 0) append("${hours}ч ")
        if (minutes > 0 || hours > 0 || days > 0) append("${minutes}мин")
        if (seconds < 60) append("${seconds % 60}сек")
    }.trim()
}

/**
 * Форматирует ISO-8601 timestamp в читаемый формат.
 * Пример входа: "2026-04-07T18:47:11.844222Z"
 * Пример выхода: "07.04.2026 21:47" (локальное время)
 */
fun formatTimestamp(isoTimestamp: String): String {
    return try {
        val trimmed = if (isoTimestamp.contains(".")) {
            val base = isoTimestamp.substringBefore(".")
            val frac = isoTimestamp.substringAfter(".").replace("Z", "")
            val ms = frac.take(3).padStart(3, '0')
            "$base.$ms"
        } else {
            isoTimestamp.replace("Z", "")
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(trimmed) ?: return isoTimestamp
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        isoTimestamp
    }
}
