package com.vennilay.kernvox.ui.utils

/**
 * Форматирует время аптайма из секунд в читаемый формат.
 *
 * @param seconds Время в секундах
 * @return Отформатированная строка (например, "2ч 30мин" или "1дн 5ч")
 */
fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60

    return buildString {
        if (days > 0) {
            append("${days}дн ")
        }
        if (hours > 0 || days > 0) {
            append("${hours}ч ")
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            append("${minutes}мин")
        }
        if (seconds < 60) {
            append("${seconds % 60}сек")
        }
    }.trim()
}
