package com.vennilay.kernvox.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.screens.servers.ServersScreenContent
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.state.UiText
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServersScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun loadingStateShowsIndicatorAndNoList() {
        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Loading,
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_list").assertDoesNotExist()
    }

    @Test
    fun errorStateShowsMessageAndRetryButton() {
        val errorMessage = context.getString(R.string.error_request_failed)

        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Error(UiText.resource(R.string.error_request_failed)),
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun retryButtonInvokesCallback() {
        var retryCalled = false

        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Error(UiText.resource(R.string.error_request_failed)),
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = { retryCalled = true },
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("error_retry_button").performClick()
        assertTrue(retryCalled)
    }

    @Test
    fun successStateWithServersShowsList() {
        val servers = listOf(
            aServer(id = 1, name = "web-prod"),
            aServer(id = 2, name = "db-main"),
        )

        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Success(servers),
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("servers_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("web-prod").assertIsDisplayed()
        composeTestRule.onNodeWithText("db-main").assertIsDisplayed()
    }

    @Test
    fun successStateWithEmptyListShowsEmptyState() {
        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Success(emptyList()),
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("empty_state").assertIsDisplayed()
    }

    @Test
    fun settingsButtonInvokesNavigationCallback() {
        var navigateCalled = false

        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Loading,
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = { navigateCalled = true },
                    onServerClick = {},
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_button").performClick()
        assertTrue(navigateCalled)
    }

    @Test
    fun serverCardClickInvokesCallbackWithCorrectServer() {
        var clickedServer: Server? = null
        val server = aServer(id = 99, name = "click-me")

        composeTestRule.setContent {
            KernvoxTheme {
                ServersScreenContent(
                    uiState = UiState.Success(listOf(server)),
                    hubOverview = null,
                    isRefreshing = false,
                    isPasswordLockEnabled = false,
                    onNavigateToSettings = {},
                    onServerClick = { clickedServer = it },
                    onRefresh = {},
                    onRetry = {},
                    onLockClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("server_card").performClick()
        assertEquals(99, clickedServer?.id)
    }

    // region Helpers

    private fun aServer(
        id: Int = 1,
        name: String = "server",
        host: String = "192.168.1.1",
        port: Int = 22,
        isAvailable: Boolean = true,
    ) = Server(
        id = id,
        name = name,
        host = host,
        port = port,
        isActive = true,
        isAvailable = isAvailable,
        cpuPercent = 10f,
        ramPercent = 20f,
        diskUsedPercent = 30f,
        cpuCores = null,
        uptimeSeconds = null,
        uptimeFormatted = null,
        ramUsedMb = null,
        ramTotalMb = null,
        networkRxBytes = null,
        networkTxBytes = null,
        lastMetricTimestamp = null,
        username = null,
    )

    // endregion
}
