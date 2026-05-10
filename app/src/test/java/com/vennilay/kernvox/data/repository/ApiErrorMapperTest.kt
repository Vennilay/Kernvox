package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorMapperTest {

    @Test
    fun mapsKnownEnglishMessagesToStringResources() {
        assertEquals(
            R.string.error_invalid_api_key,
            userFriendlyApiMessageRes("Invalid API key"),
        )
        assertEquals(
            R.string.error_action_key_required,
            userFriendlyApiMessageRes("Server action key is required"),
        )
    }

    @Test
    fun hidesUnknownEnglishTechnicalMessages() {
        assertEquals(
            R.string.error_request_failed,
            userFriendlyApiMessageRes("Internal Server Error: No running event loop"),
        )
    }

    @Test
    fun mapsRussianServerMessagesToResources() {
        assertEquals(
            R.string.error_server_not_found,
            userFriendlyApiMessageRes("Сервер не найден."),
        )
    }
}
