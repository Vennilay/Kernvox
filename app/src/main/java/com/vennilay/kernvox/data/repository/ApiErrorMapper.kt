package com.vennilay.kernvox.data.repository

import java.util.Locale

private const val DEFAULT_REQUEST_ERROR =
    "Не удалось выполнить запрос. Проверьте подключение и повторите."

fun userFriendlyApiMessage(message: String?): String {
    val rawMessage = message?.trim().orEmpty()
    if (rawMessage.isBlank()) return DEFAULT_REQUEST_ERROR

    val normalized = rawMessage.lowercase(Locale.ROOT)
    return when {
        normalized.contains("invalid api key") ||
            normalized.contains("unauthorized") ||
            normalized.contains("неверный api-ключ") ->
            "Неверный API-ключ. Проверьте настройки подключения."

        normalized.contains("server action key is required") ||
            normalized.contains("x-action-key") ->
            "Для перезагрузки нужен X-Action-Key. Укажите его в настройках."

        normalized.contains("forbidden") ->
            "Недостаточно прав для выполнения действия."

        normalized.contains("not found") ||
            normalized.contains("сервер не найден") ->
            "Сервер не найден."

        normalized.contains("too many requests") ||
            normalized.contains("слишком много запросов") ->
            "Слишком много запросов. Повторите позже."

        normalized.contains("timeout") ||
            normalized.contains("timed out") ||
            normalized.contains("таймаут") ||
            normalized.contains("превышено время") ->
            "Не удалось подключиться к серверу."

        normalized.contains("unknownhost") ||
            normalized.contains("failed to connect") ||
            normalized.contains("connection refused") ->
            "Не удалось подключиться к серверу."

        normalized.contains("service unavailable") ||
            normalized.contains("temporarily unavailable") ->
            "Сервер временно недоступен."

        normalized.contains("serialization") ||
            normalized.contains("unexpected json") ||
            normalized.contains("format") ->
            "Не удалось прочитать ответ сервера."

        rawMessage.hasLatinLetters() ->
            DEFAULT_REQUEST_ERROR

        else -> rawMessage
    }
}

fun Throwable.toUserFriendlyMessage(defaultMessage: String = DEFAULT_REQUEST_ERROR): String =
    userFriendlyApiMessage(message ?: defaultMessage)

private fun String.hasLatinLetters(): Boolean = any { it in 'A'..'Z' || it in 'a'..'z' }
