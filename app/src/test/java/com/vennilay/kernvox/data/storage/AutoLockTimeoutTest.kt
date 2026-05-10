package com.vennilay.kernvox.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoLockTimeoutTest {

    @Test
    fun defaultsToFiveMinutesForMissingOrUnknownValue() {
        assertEquals(AutoLockTimeout.FIVE_MINUTES, AutoLockTimeout.fromStorageValue(null))
        assertEquals(AutoLockTimeout.FIVE_MINUTES, AutoLockTimeout.fromStorageValue("unknown"))
    }

    @Test
    fun mapsStoredValuesToTimeouts() {
        AutoLockTimeout.entries.forEach { timeout ->
            assertEquals(timeout, AutoLockTimeout.fromStorageValue(timeout.storageValue))
        }
    }

    @Test
    fun locksWhenElapsedTimeReachesSelectedTimeout() {
        assertTrue(AutoLockTimeout.IMMEDIATE.shouldLockAfter(0L))
        assertFalse(AutoLockTimeout.FIVE_MINUTES.shouldLockAfter(5 * 60 * 1000L - 1))
        assertTrue(AutoLockTimeout.FIVE_MINUTES.shouldLockAfter(5 * 60 * 1000L))
    }
}
