package com.vennilay.kernvox.data.repository

import com.vennilay.kernvox.data.model.Server
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeServersRepository : ServersRepository {
    private val mutex = Mutex()

    private val servers = mutableListOf(
        Server(
            id = "srv-1",
            name = "Main Gateway",
            host = "192.168.1.1",
            port = 8080,
            uptimeSeconds = 125_340,
            isAvailable = true,
            lastCheckedAtEpochMillis = 1_700_000_000_000,
        ),
        Server(
            id = "srv-2",
            name = "Monitoring Node",
            host = "monitoring.local",
            port = 8080,
            uptimeSeconds = 40_120,
            isAvailable = true,
            lastCheckedAtEpochMillis = 1_700_000_050_000,
        ),
        Server(
            id = "srv-3",
            name = "DB Host",
            host = "10.0.0.10",
            port = 5432,
            uptimeSeconds = 0,
            isAvailable = false,
            lastCheckedAtEpochMillis = 1_700_000_100_000,
        ),
    )

    override suspend fun getServers(): List<Server> {
        return mutex.withLock { servers.toList() }
    }

    override suspend fun addServer(server: Server) {
        mutex.withLock {
            servers.add(server)
        }
    }
}
