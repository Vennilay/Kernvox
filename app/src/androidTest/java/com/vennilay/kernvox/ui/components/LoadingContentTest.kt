package com.vennilay.kernvox.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import org.junit.Rule
import org.junit.Test

class LoadingContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingIndicatorIsDisplayed() {
        composeTestRule.setContent {
            KernvoxTheme {
                LoadingContent(paddingValues = PaddingValues(0.dp))
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}
