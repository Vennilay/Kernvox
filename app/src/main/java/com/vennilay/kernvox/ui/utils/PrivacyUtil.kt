package com.vennilay.kernvox.ui.utils

/**
 * Утилита для скрытия чувствительных данных (IP, хост, имя пользователя).
 */
object PrivacyUtil {

    /**
     * Скрывает часть IP-адреса или хоста.
     * Пример: 192.168.1.1 -> 192.xxx.xxx.1
     * Пример: example.com -> ex***le.com
     */
    fun maskHost(host: String): String {
        if (host.isEmpty()) return host

        // Если это IP-адрес (простая проверка)
        if (host.matches(Regex("""^(\d{1,3}\.){3}\d{1,3}$"""))) {
            val parts = host.split(".")
            if (parts.size == 4) {
                return "${parts[0]}.xxx.xxx.${parts[3]}"
            }
        }

        // Если это домен
        val dotIndex = host.lastIndexOf(".")
        val name = if (dotIndex != -1) host.substring(0, dotIndex) else host
        val domain = if (dotIndex != -1) host.substring(dotIndex) else ""

        return if (name.length <= 2) {
            "**$domain"
        } else {
            "${name.take(2)}***${name.takeLast(2)}$domain"
        }
    }

    /**
     * Скрывает часть имени пользователя.
     * Пример: admin -> a***n
     */
    fun maskUsername(username: String): String {
        if (username.length <= 2) return "***"
        return "${username.first()}***${username.last()}"
    }
}
