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

    @Test
    fun mapsNullAndBlankToRequestFailed() {
        assertEquals(R.string.error_request_failed, userFriendlyApiMessageRes(null))
        assertEquals(R.string.error_request_failed, userFriendlyApiMessageRes(""))
        assertEquals(R.string.error_request_failed, userFriendlyApiMessageRes("   "))
    }

    @Test
    fun mapsForbiddenToForbiddenError() {
        assertEquals(R.string.error_forbidden, userFriendlyApiMessageRes("Forbidden"))
        assertEquals(R.string.error_forbidden, userFriendlyApiMessageRes("403 Forbidden"))
    }

    @Test
    fun mapsTooManyRequestsToRateLimitError() {
        assertEquals(R.string.error_too_many_requests, userFriendlyApiMessageRes("Too Many Requests"))
        assertEquals(R.string.error_too_many_requests, userFriendlyApiMessageRes("Слишком много запросов"))
    }

    @Test
    fun mapsConnectionErrorsToConnectionFailed() {
        assertEquals(R.string.error_connection_failed, userFriendlyApiMessageRes("Connection timeout"))
        assertEquals(R.string.error_connection_failed, userFriendlyApiMessageRes("Request timed out"))
        assertEquals(R.string.error_connection_failed, userFriendlyApiMessageRes("Connection refused"))
        assertEquals(R.string.error_connection_failed, userFriendlyApiMessageRes("Failed to connect"))
    }

    @Test
    fun mapsServiceUnavailableToUnavailableError() {
        assertEquals(R.string.error_service_unavailable, userFriendlyApiMessageRes("Service Unavailable"))
        assertEquals(R.string.error_service_unavailable, userFriendlyApiMessageRes("Temporarily Unavailable"))
    }

    @Test
    fun mapsSerializationErrorsToFormatError() {
        assertEquals(R.string.error_response_format, userFriendlyApiMessageRes("Unexpected JSON token"))
        assertEquals(R.string.error_response_format, userFriendlyApiMessageRes("Serialization failed"))
    }

    @Test
    fun throwableExtensionDelegatesToMessageMapping() {
        val exception = RuntimeException("Invalid API key")
        assertEquals(R.string.error_invalid_api_key, exception.toUserFriendlyMessageRes())
    }

    @Test
    fun throwableExtensionUsesDefaultResIdForUnknownErrors() {
        val exception = RuntimeException("Some internal error with no mapping")
        assertEquals(R.string.error_request_failed, exception.toUserFriendlyMessageRes())
    }
}
