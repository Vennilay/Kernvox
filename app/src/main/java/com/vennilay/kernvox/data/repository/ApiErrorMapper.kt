package com.vennilay.kernvox.data.repository

import androidx.annotation.StringRes
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.network.InsecureServerUrlException
import java.util.Locale

/**
 * Преобразует подробности ошибок сети, API и SSH в стабильные строковые ресурсы для удобного отображения сообщений в пользовательском интерфейсе.
 *
 * Исходные сообщения бэкэнда и тексты исключений регистрируются в журнале репозитория, но пользовательский интерфейс получает только
 * короткие локализованные ресурсы, поэтому трассировки стека, внутренние английские тексты и подробности SSH не отображаются.
 */
@StringRes
fun userFriendlyApiMessageRes(message: String?): Int {
    val rawMessage = message?.trim().orEmpty()
    if (rawMessage.isBlank()) return R.string.error_request_failed

    val normalized = rawMessage.lowercase(Locale.ROOT)
    return when {
        normalized.contains("invalid api key") ||
            normalized.contains("unauthorized") ||
            normalized.contains("неверный api-ключ") ->
            R.string.error_invalid_api_key

        normalized.contains("server action key is required") ||
            normalized.contains("x-action-key") ->
            R.string.error_action_key_required

        normalized.contains("forbidden") ->
            R.string.error_forbidden

        normalized.contains("not found") ||
            normalized.contains("сервер не найден") ->
            R.string.error_server_not_found

        normalized.contains("too many requests") ||
            normalized.contains("слишком много запросов") ->
            R.string.error_too_many_requests

        normalized.contains("timeout") ||
            normalized.contains("timed out") ||
            normalized.contains("таймаут") ||
            normalized.contains("превышено время") ->
            R.string.error_connection_failed

        normalized.contains("unknownhost") ||
            normalized.contains("failed to connect") ||
            normalized.contains("connection refused") ->
            R.string.error_connection_failed

        normalized.contains("service unavailable") ||
            normalized.contains("temporarily unavailable") ->
            R.string.error_service_unavailable

        normalized.contains("serialization") ||
            normalized.contains("unexpected json") ||
            normalized.contains("format") ->
            R.string.error_response_format

        else -> R.string.error_request_failed
    }
}

@StringRes
fun Throwable.toUserFriendlyMessageRes(@StringRes defaultResId: Int = R.string.error_request_failed): Int =
    when (this) {
        is ApiException -> messageResId
        is InsecureServerUrlException -> R.string.error_release_https_required
        else -> userFriendlyApiMessageRes(message).takeUnless { it == R.string.error_request_failed }
            ?: defaultResId
    }
