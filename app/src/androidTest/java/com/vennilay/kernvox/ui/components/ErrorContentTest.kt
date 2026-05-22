package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ErrorContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorMessageIsDisplayed() {
        composeTestRule.setContent {
            KernvoxTheme {
                ErrorContent(
                    title = "Error Title",
                    message = "Something went wrong",
                    retryLabel = "Retry",
                    onRetry = {},
                    paddingValues = PaddingValues(0.dp),
                )
            }
        }

        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun retryButtonIsDisplayed() {
        composeTestRule.setContent {
            KernvoxTheme {
                ErrorContent(
                    title = "Error",
                    message = "Failed",
                    retryLabel = "Try again",
                    onRetry = {},
                    paddingValues = PaddingValues(0.dp),
                )
            }
        }

        composeTestRule.onNodeWithTag("error_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun retryButtonInvokesCallback() {
        var retryClicked = false

        composeTestRule.setContent {
            KernvoxTheme {
                ErrorContent(
                    title = "Error",
                    message = "Failed",
                    retryLabel = "Retry",
                    onRetry = { retryClicked = true },
                    paddingValues = PaddingValues(0.dp),
                )
            }
        }

        composeTestRule.onNodeWithTag("error_retry_button").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun titleIsDisplayed() {
        composeTestRule.setContent {
            KernvoxTheme {
                ErrorContent(
                    title = "Connection Failed",
                    message = "Check your network",
                    retryLabel = "Retry",
                    onRetry = {},
                    paddingValues = PaddingValues(0.dp),
                )
            }
        }

        composeTestRule.onNodeWithText("Connection Failed").assertIsDisplayed()
    }
}
