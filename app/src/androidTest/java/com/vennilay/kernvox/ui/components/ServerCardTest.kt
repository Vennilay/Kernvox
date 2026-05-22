package com.vennilay.kernvox.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ServerCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun cardIsDisplayed() {
        composeTestRule.setContent {
            KernvoxTheme {
                ServerCard(server = anOnlineServer())
            }
        }

        composeTestRule.onNodeWithTag("server_card").assertIsDisplayed()
    }

    @Test
    fun onlineServerShowsNameAndHost() {
        val server = anOnlineServer(name = "web-prod", host = "10.0.0.1", port = 22)

        composeTestRule.setContent {
            KernvoxTheme { ServerCard(server = server) }
        }

        composeTestRule.onNodeWithText("web-prod").assertIsDisplayed()
        composeTestRule.onNodeWithText("10.0.0.1:22").assertIsDisplayed()
    }

    @Test
    fun onlineServerShowsMetricLabels() {
        composeTestRule.setContent {
            KernvoxTheme { ServerCard(server = anOnlineServer()) }
        }

        val cpuLabel = context.getString(R.string.server_card_cpu_label)
        val ramLabel = context.getString(R.string.server_card_ram_label)
        val diskLabel = context.getString(R.string.server_card_disk_label)

        composeTestRule.onNodeWithText(cpuLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(ramLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(diskLabel).assertIsDisplayed()
    }

    @Test
    fun offlineServerShowsOfflineLabel() {
        composeTestRule.setContent {
            KernvoxTheme { ServerCard(server = anOfflineServer()) }
        }

        val offlineLabel = context.getString(R.string.servers_status_offline)
        composeTestRule.onNodeWithText(offlineLabel).assertIsDisplayed()
    }

    @Test
    fun offlineServerDoesNotShowMetricLabels() {
        composeTestRule.setContent {
            KernvoxTheme { ServerCard(server = anOfflineServer()) }
        }

        val cpuLabel = context.getString(R.string.server_card_cpu_label)
        composeTestRule.onNodeWithText(cpuLabel).assertDoesNotExist()
    }

    @Test
    fun cardClickInvokesCallback() {
        var clickedServer: Server? = null
        val server = anOnlineServer(id = 42)

        composeTestRule.setContent {
            KernvoxTheme {
                ServerCard(server = server, onClick = { clickedServer = it })
            }
        }

        composeTestRule.onNodeWithTag("server_card").performClick()
        assertEquals(42, clickedServer?.id)
    }

    @Test
    fun onlineServerShowsFormattedCpuValue() {
        val server = anOnlineServer(cpuPercent = 75.5f)

        composeTestRule.setContent {
            KernvoxTheme { ServerCard(server = server) }
        }

        composeTestRule.onNodeWithText("75.5%").assertIsDisplayed()
    }

    // region Helpers

    private fun anOnlineServer(
        id: Int = 1,
        name: String = "web-01",
        host: String = "192.168.1.1",
        port: Int = 22,
        cpuPercent: Float? = 25.0f,
        ramPercent: Float? = 50.0f,
        diskUsedPercent: Float? = 40.0f,
    ) = Server(
        id = id,
        name = name,
        host = host,
        port = port,
        isActive = true,
        isAvailable = true,
        cpuPercent = cpuPercent,
        ramPercent = ramPercent,
        diskUsedPercent = diskUsedPercent,
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

    private fun anOfflineServer(
        id: Int = 2,
        name: String = "db-01",
        host: String = "192.168.1.2",
        port: Int = 22,
    ) = Server(
        id = id,
        name = name,
        host = host,
        port = port,
        isActive = true,
        isAvailable = false,
        cpuPercent = null,
        ramPercent = null,
        diskUsedPercent = null,
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
