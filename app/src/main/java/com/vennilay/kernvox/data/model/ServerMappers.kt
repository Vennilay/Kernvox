package com.vennilay.kernvox.data.model

import com.vennilay.kernvox.data.network.dto.DashboardServerDto
import com.vennilay.kernvox.data.network.dto.MetricEntryDto
import com.vennilay.kernvox.data.network.dto.ProcessInfoDto
import com.vennilay.kernvox.data.network.dto.ServerActionResponseDto
import com.vennilay.kernvox.data.network.dto.ServerDetailsDto
import java.net.URI

fun DashboardServerDto.toServer(): Server {
    return Server(
        id = id,
        name = name,
        host = host,
        port = port ?: 22,
        isActive = isActive,
        isAvailable = isAvailable,
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
        lastMetricTimestamp = lastUpdate,
        username = null,
    )
}

fun ServerDetailsDto.toServer(): Server {
    return Server(
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
}

fun Server.mergeWith(details: Server): Server {
    return copy(
        port = details.port,
        cpuPercent = details.cpuPercent ?: cpuPercent,
        ramPercent = details.ramPercent ?: ramPercent,
        diskUsedPercent = details.diskUsedPercent ?: diskUsedPercent,
        cpuCores = details.cpuCores ?: cpuCores,
        uptimeSeconds = details.uptimeSeconds ?: uptimeSeconds,
        uptimeFormatted = details.uptimeFormatted ?: uptimeFormatted,
        ramUsedMb = details.ramUsedMb ?: ramUsedMb,
        ramTotalMb = details.ramTotalMb ?: ramTotalMb,
        networkRxBytes = details.networkRxBytes ?: networkRxBytes,
        networkTxBytes = details.networkTxBytes ?: networkTxBytes,
        lastMetricTimestamp = details.lastMetricTimestamp ?: lastMetricTimestamp,
        username = details.username ?: username,
        isAvailable = details.isAvailable ?: isAvailable,
    )
}

fun ProcessInfoDto.toProcess(): Process = Process(
    pid = pid,
    user = user,
    cpuPercent = cpuPercent,
    memoryPercent = memoryPercent,
    command = command,
)

fun MetricEntryDto.toMetricEntry(): MetricEntry = MetricEntry(
    id = id,
    cpuPercent = cpuPercent,
    ramUsedMb = ramUsedMb,
    ramTotalMb = ramTotalMb,
    ramPercent = ramPercent,
    diskUsedPercent = diskUsedPercent,
    networkRxBytes = networkRxBytes,
    networkTxBytes = networkTxBytes,
    uptimeSeconds = uptimeSeconds,
    isAvailable = isAvailable,
    timestamp = timestamp,
)

fun ServerActionResponseDto.toServerActionResult(): ServerActionResult = ServerActionResult(
    action = action,
    status = status,
    message = message,
    createdAt = createdAt,
)

fun List<Server>.toHubOverview(baseUrl: String): HubOverview {
    val parsedUri = runCatching { URI(baseUrl) }.getOrNull()
    val host = parsedUri?.host ?: baseUrl.removePrefix("https://").removePrefix("http://")
    val port = parsedUri?.port?.takeIf { it >= 0 }
    return HubOverview(
        name = "KernvoxHub",
        baseUrl = baseUrl,
        host = host,
        port = port,
        totalNodes = size,
        availableNodes = count { it.isAvailable == true },
        lastUpdate = mapNotNull { it.lastMetricTimestamp }.maxOrNull(),
    )
}
