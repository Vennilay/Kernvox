package com.vennilay.kernvox.data.model

import com.vennilay.kernvox.data.network.dto.DashboardServerDto
import com.vennilay.kernvox.data.network.dto.ServerDetailsDto

/**
 * Маппинг из DashboardServerDto в domain-модель Server.
 * Используется для списка серверов на главном экране.
 */
fun DashboardServerDto.toServer(): Server {
    return Server(
        id = id,
        name = name,
        host = host,
        port = 22,
        isActive = is_active,
        isAvailable = is_available,
        cpuPercent = cpu_percent,
        ramPercent = ram_percent,
        diskUsedPercent = disk_used_percent,
        uptimeSeconds = null,
        uptimeFormatted = null,
        ramUsedMb = null,
        ramTotalMb = null,
        networkRxBytes = null,
        networkTxBytes = null,
        lastMetricTimestamp = last_update,
        username = null,
    )
}

/**
 * Маппинг из ServerDetailsDto в domain-модель Server.
 * Используется для экрана детальной информации.
 */
fun ServerDetailsDto.toServer(): Server {
    return Server(
        id = id,
        name = name,
        host = host,
        port = port,
        isActive = is_active,
        isAvailable = is_available,
        cpuPercent = cpu_percent,
        ramPercent = ram_percent,
        diskUsedPercent = disk_used_percent,
        uptimeSeconds = uptime_seconds,
        uptimeFormatted = uptime_formatted,
        ramUsedMb = ram_used_mb,
        ramTotalMb = ram_total_mb,
        networkRxBytes = network_rx_bytes,
        networkTxBytes = network_tx_bytes,
        lastMetricTimestamp = last_metric_timestamp,
        username = username,
    )
}
