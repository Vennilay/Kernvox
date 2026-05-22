package com.vennilay.kernvox.data.model

import com.vennilay.kernvox.data.network.dto.DashboardServerDto
import com.vennilay.kernvox.data.network.dto.MetricEntryDto
import com.vennilay.kernvox.data.network.dto.ProcessInfoDto
import com.vennilay.kernvox.data.network.dto.ServerActionResponseDto
import com.vennilay.kernvox.data.network.dto.ServerDetailsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerMappersTest {

    // region DashboardServerDto.toServer()

    @Test
    fun dashboardDtoMapsAllBasicFields() {
        val dto = aDashboardDto(
            id = 42,
            name = "web-01",
            host = "192.168.1.10",
            isActive = true,
            isAvailable = true,
            cpuPercent = 45.5f,
            ramPercent = 62.3f,
            diskUsedPercent = 38.1f,
            lastUpdate = "2026-01-01T12:00:00.000Z",
        )

        val server = dto.toServer()

        assertEquals(42, server.id)
        assertEquals("web-01", server.name)
        assertEquals("192.168.1.10", server.host)
        assertEquals(true, server.isActive)
        assertEquals(true, server.isAvailable)
        assertEquals(45.5f, server.cpuPercent)
        assertEquals(62.3f, server.ramPercent)
        assertEquals(38.1f, server.diskUsedPercent)
        assertEquals("2026-01-01T12:00:00.000Z", server.lastMetricTimestamp)
    }

    @Test
    fun dashboardDtoAlwaysSetPortTo22() {
        val server = aDashboardDto().toServer()
        assertEquals(22, server.port)
    }

    @Test
    fun dashboardDtoLeavesDetailOnlyFieldsNull() {
        val server = aDashboardDto().toServer()

        assertNull(server.cpuCores)
        assertNull(server.uptimeSeconds)
        assertNull(server.uptimeFormatted)
        assertNull(server.ramUsedMb)
        assertNull(server.ramTotalMb)
        assertNull(server.networkRxBytes)
        assertNull(server.networkTxBytes)
        assertNull(server.username)
    }

    @Test
    fun dashboardDtoHandlesNullOptionalFields() {
        val dto = aDashboardDto(
            isAvailable = null,
            cpuPercent = null,
            ramPercent = null,
            diskUsedPercent = null,
            lastUpdate = null,
        )

        val server = dto.toServer()

        assertNull(server.isAvailable)
        assertNull(server.cpuPercent)
        assertNull(server.ramPercent)
        assertNull(server.diskUsedPercent)
        assertNull(server.lastMetricTimestamp)
    }

    // endregion

    // region ServerDetailsDto.toServer()

    @Test
    fun detailsDtoMapsAllFields() {
        val dto = aDetailsDto(
            id = 7,
            name = "db-01",
            host = "10.0.0.5",
            port = 2222,
            username = "admin",
            isActive = true,
            isAvailable = true,
            cpuCores = 4,
            uptimeSeconds = 86400f,
            uptimeFormatted = "1d",
            cpuPercent = 12.5f,
            ramUsedMb = 2048f,
            ramTotalMb = 8192f,
            ramPercent = 25f,
            diskUsedPercent = 55f,
            networkRxBytes = 1024f,
            networkTxBytes = 512f,
            lastMetricTimestamp = "2026-01-01T00:00:00.000Z",
        )

        val server = dto.toServer()

        assertEquals(7, server.id)
        assertEquals("db-01", server.name)
        assertEquals("10.0.0.5", server.host)
        assertEquals(2222, server.port)
        assertEquals("admin", server.username)
        assertEquals(4, server.cpuCores)
        assertEquals(86400f, server.uptimeSeconds)
        assertEquals("1d", server.uptimeFormatted)
        assertEquals(12.5f, server.cpuPercent)
        assertEquals(2048f, server.ramUsedMb)
        assertEquals(8192f, server.ramTotalMb)
        assertEquals(25f, server.ramPercent)
        assertEquals(55f, server.diskUsedPercent)
        assertEquals(1024f, server.networkRxBytes)
        assertEquals(512f, server.networkTxBytes)
        assertEquals("2026-01-01T00:00:00.000Z", server.lastMetricTimestamp)
    }

    // endregion

    // region Server.mergeWith()

    @Test
    fun mergeOverridesBaseWithDetailsValues() {
        val base = aServer(cpuPercent = 10f, ramPercent = 20f, diskUsedPercent = 30f)
        val details = aServer(cpuPercent = 90f, ramPercent = 80f, diskUsedPercent = 70f, port = 2222)

        val merged = base.mergeWith(details)

        assertEquals(90f, merged.cpuPercent)
        assertEquals(80f, merged.ramPercent)
        assertEquals(70f, merged.diskUsedPercent)
        assertEquals(2222, merged.port)
    }

    @Test
    fun mergeFallsBackToBaseWhenDetailsFieldIsNull() {
        val base = aServer(cpuPercent = 50f, ramPercent = 40f, username = "root")
        val details = aServer(cpuPercent = null, ramPercent = null, username = null)

        val merged = base.mergeWith(details)

        assertEquals(50f, merged.cpuPercent)
        assertEquals(40f, merged.ramPercent)
        assertEquals("root", merged.username)
    }

    @Test
    fun mergeAlwaysUpdatesPort() {
        val base = aServer(port = 22)
        val details = aServer(port = 9022)

        val merged = base.mergeWith(details)

        assertEquals(9022, merged.port)
    }

    // endregion

    // region ProcessInfoDto.toProcess()

    @Test
    fun processInfoDtoMapsAllFields() {
        val dto = ProcessInfoDto(
            pid = 1234,
            user = "www-data",
            cpuPercent = 2.5f,
            memoryPercent = 1.1f,
            command = "nginx: worker",
        )

        val process = dto.toProcess()

        assertEquals(1234, process.pid)
        assertEquals("www-data", process.user)
        assertEquals(2.5f, process.cpuPercent)
        assertEquals(1.1f, process.memoryPercent)
        assertEquals("nginx: worker", process.command)
    }

    // endregion

    // region MetricEntryDto.toMetricEntry()

    @Test
    fun metricEntryDtoMapsAllFields() {
        val dto = MetricEntryDto(
            id = 999,
            cpuPercent = 33.3f,
            ramUsedMb = 1024f,
            ramTotalMb = 4096f,
            ramPercent = 25f,
            diskUsedPercent = 60f,
            networkRxBytes = 2048f,
            networkTxBytes = 512f,
            uptimeSeconds = 3600f,
            isAvailable = true,
            timestamp = "2026-04-01T10:00:00.000Z",
        )

        val entry = dto.toMetricEntry()

        assertEquals(999, entry.id)
        assertEquals(33.3f, entry.cpuPercent)
        assertEquals(1024f, entry.ramUsedMb)
        assertEquals(4096f, entry.ramTotalMb)
        assertEquals(25f, entry.ramPercent)
        assertEquals(60f, entry.diskUsedPercent)
        assertEquals(2048f, entry.networkRxBytes)
        assertEquals(512f, entry.networkTxBytes)
        assertEquals(3600f, entry.uptimeSeconds)
        assertEquals(true, entry.isAvailable)
        assertEquals("2026-04-01T10:00:00.000Z", entry.timestamp)
    }

    // endregion

    // region ServerActionResponseDto.toServerActionResult()

    @Test
    fun serverActionResponseDtoMapsAllFields() {
        val dto = ServerActionResponseDto(
            id = 1,
            serverId = 5,
            serverName = "web-01",
            action = "reboot",
            status = "accepted",
            message = "Reboot scheduled",
            createdAt = "2026-01-01T00:00:00.000Z",
        )

        val result = dto.toServerActionResult()

        assertEquals("reboot", result.action)
        assertEquals("accepted", result.status)
        assertEquals("Reboot scheduled", result.message)
        assertEquals("2026-01-01T00:00:00.000Z", result.createdAt)
    }

    @Test
    fun serverActionResponseDtoHandlesNullMessage() {
        val dto = ServerActionResponseDto(
            id = 2,
            serverId = 5,
            serverName = "web-01",
            action = "reboot",
            status = "queued",
            message = null,
            createdAt = "2026-01-01T00:00:00.000Z",
        )

        assertNull(dto.toServerActionResult().message)
    }

    // endregion

    // region List<Server>.toHubOverview()

    @Test
    fun hubOverviewExtractsHostAndPortFromValidUrl() {
        val overview = emptyList<Server>().toHubOverview("https://kernvox.example.com:8080")

        assertEquals("kernvox.example.com", overview.host)
        assertEquals(8080, overview.port)
        assertEquals("https://kernvox.example.com:8080", overview.baseUrl)
    }

    @Test
    fun hubOverviewExtractsHostWithoutPortFromUrl() {
        val overview = emptyList<Server>().toHubOverview("https://kernvox.example.com")

        assertEquals("kernvox.example.com", overview.host)
        assertNull(overview.port)
    }

    @Test
    fun hubOverviewFallsBackToStrippedHostOnMalformedUrl() {
        val overview = emptyList<Server>().toHubOverview("not-a-url")

        assertEquals("not-a-url", overview.host)
    }

    @Test
    fun hubOverviewCountsOnlyAvailableNodes() {
        val servers = listOf(
            aServer(id = 1, isAvailable = true),
            aServer(id = 2, isAvailable = false),
            aServer(id = 3, isAvailable = true),
            aServer(id = 4, isAvailable = null),
        )

        val overview = servers.toHubOverview("http://localhost")

        assertEquals(4, overview.totalNodes)
        assertEquals(2, overview.availableNodes)
    }

    @Test
    fun hubOverviewUsesMaxTimestampAsLastUpdate() {
        val servers = listOf(
            aServer(id = 1, lastMetricTimestamp = "2026-01-01T12:00:00Z"),
            aServer(id = 2, lastMetricTimestamp = "2026-01-01T10:00:00Z"),
            aServer(id = 3, lastMetricTimestamp = "2026-01-01T11:00:00Z"),
        )

        val overview = servers.toHubOverview("http://localhost")

        assertEquals("2026-01-01T12:00:00Z", overview.lastUpdate)
    }

    @Test
    fun hubOverviewHandlesNullTimestampsGracefully() {
        val servers = listOf(
            aServer(id = 1, lastMetricTimestamp = null),
            aServer(id = 2, lastMetricTimestamp = "2026-01-01T12:00:00Z"),
        )

        val overview = servers.toHubOverview("http://localhost")

        assertEquals("2026-01-01T12:00:00Z", overview.lastUpdate)
    }

    @Test
    fun hubOverviewReturnsNullLastUpdateWhenAllTimestampsAreNull() {
        val servers = listOf(aServer(id = 1, lastMetricTimestamp = null))

        val overview = servers.toHubOverview("http://localhost")

        assertNull(overview.lastUpdate)
    }

    @Test
    fun emptyServerListProducesZeroCounts() {
        val overview = emptyList<Server>().toHubOverview("http://localhost")

        assertEquals(0, overview.totalNodes)
        assertEquals(0, overview.availableNodes)
        assertNull(overview.lastUpdate)
    }

    // endregion

    // region Helpers

    private fun aDashboardDto(
        id: Int = 1,
        name: String = "server",
        host: String = "192.168.1.1",
        isActive: Boolean = true,
        isAvailable: Boolean? = true,
        cpuPercent: Float? = 10f,
        ramPercent: Float? = 20f,
        diskUsedPercent: Float? = 30f,
        lastUpdate: String? = "2026-01-01T00:00:00.000Z",
    ) = DashboardServerDto(
        id = id,
        name = name,
        host = host,
        isActive = isActive,
        isAvailable = isAvailable,
        cpuPercent = cpuPercent,
        ramPercent = ramPercent,
        diskUsedPercent = diskUsedPercent,
        lastUpdate = lastUpdate,
    )

    private fun aDetailsDto(
        id: Int = 1,
        name: String = "server",
        host: String = "192.168.1.1",
        port: Int = 22,
        username: String = "ubuntu",
        isActive: Boolean = true,
        isAvailable: Boolean? = true,
        cpuCores: Int? = null,
        uptimeSeconds: Float? = null,
        uptimeFormatted: String? = null,
        cpuPercent: Float? = null,
        ramUsedMb: Float? = null,
        ramTotalMb: Float? = null,
        ramPercent: Float? = null,
        diskUsedPercent: Float? = null,
        networkRxBytes: Float? = null,
        networkTxBytes: Float? = null,
        lastMetricTimestamp: String? = null,
    ) = ServerDetailsDto(
        id = id,
        name = name,
        host = host,
        port = port,
        username = username,
        isActive = isActive,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = null,
        cpuCores = cpuCores,
        uptimeSeconds = uptimeSeconds,
        uptimeFormatted = uptimeFormatted,
        cpuPercent = cpuPercent,
        ramUsedMb = ramUsedMb,
        ramTotalMb = ramTotalMb,
        ramPercent = ramPercent,
        diskUsedPercent = diskUsedPercent,
        networkRxBytes = networkRxBytes,
        networkTxBytes = networkTxBytes,
        isAvailable = isAvailable,
        lastMetricTimestamp = lastMetricTimestamp,
    )

    private fun aServer(
        id: Int = 1,
        name: String = "server",
        host: String = "192.168.1.1",
        port: Int = 22,
        isActive: Boolean = true,
        isAvailable: Boolean? = true,
        cpuPercent: Float? = null,
        ramPercent: Float? = null,
        diskUsedPercent: Float? = null,
        cpuCores: Int? = null,
        uptimeSeconds: Float? = null,
        uptimeFormatted: String? = null,
        ramUsedMb: Float? = null,
        ramTotalMb: Float? = null,
        networkRxBytes: Float? = null,
        networkTxBytes: Float? = null,
        lastMetricTimestamp: String? = null,
        username: String? = null,
    ) = Server(
        id = id,
        name = name,
        host = host,
        port = port,
        isActive = isActive,
        isAvailable = isAvailable,
        cpuPercent = cpuPercent,
        ramPercent = ramPercent,
        diskUsedPercent = diskUsedPercent,
        cpuCores = cpuCores,
        uptimeSeconds = uptimeSeconds,
        uptimeFormatted = uptimeFormatted,
        ramUsedMb = ramUsedMb,
        ramTotalMb = ramTotalMb,
        networkRxBytes = networkRxBytes,
        networkTxBytes = networkTxBytes,
        lastMetricTimestamp = lastMetricTimestamp,
        username = username,
    )

    // endregion
}
