package com.vennilay.kernvox.data.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerUrlValidatorTest {

    @Test
    fun releaseBuildAllowsHttps() {
        assertTrue(ServerUrlValidator.isAllowed("https://kernvox.example.com", isDebugBuild = false))
    }

    @Test
    fun releaseBuildBlocksPlainHttpForRegularHosts() {
        assertFalse(ServerUrlValidator.isAllowed("http://kernvox.example.com", isDebugBuild = false))
    }

    @Test
    fun debugBuildAllowsPlainHttp() {
        assertTrue(ServerUrlValidator.isAllowed("http://kernvox.example.com", isDebugBuild = true))
    }

    @Test
    fun releaseBuildAllowsLocalDevelopmentHttpHosts() {
        assertTrue(ServerUrlValidator.isAllowed("http://localhost:8080", isDebugBuild = false))
        assertTrue(ServerUrlValidator.isAllowed("http://127.0.0.1:8080", isDebugBuild = false))
        assertTrue(ServerUrlValidator.isAllowed("http://10.0.2.2:8080", isDebugBuild = false))
    }
}
