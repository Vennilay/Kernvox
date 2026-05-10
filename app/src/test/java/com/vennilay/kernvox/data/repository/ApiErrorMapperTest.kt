package com.vennilay.kernvox.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ApiErrorMapperTest {

    @Test
    fun mapsKnownEnglishMessagesToRussian() {
        assertEquals(
            "Неверный API-ключ. Проверьте настройки подключения.",
            userFriendlyApiMessage("Invalid API key"),
        )
        assertEquals(
            "Для перезагрузки нужен X-Action-Key. Укажите его в настройках.",
            userFriendlyApiMessage("Server action key is required"),
        )
    }

    @Test
    fun hidesUnknownEnglishTechnicalMessages() {
        val result = userFriendlyApiMessage("Internal Server Error: No running event loop")

        assertEquals(
            "Не удалось выполнить запрос. Проверьте подключение и повторите.",
            result,
        )
        assertFalse(result.any { it in 'A'..'Z' || it in 'a'..'z' })
    }

    @Test
    fun keepsRussianMessagesAsIs() {
        assertEquals(
            "Настройки не указаны.",
            userFriendlyApiMessage("Настройки не указаны."),
        )
    }
}
