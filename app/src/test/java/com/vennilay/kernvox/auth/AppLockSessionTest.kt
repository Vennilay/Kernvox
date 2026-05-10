package com.vennilay.kernvox.auth

import com.vennilay.kernvox.data.storage.AutoLockTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockSessionTest {

    @Test
    fun configurationChangeDoesNotLockUnlockedSession() {
        var now = 1_000L
        val session = AppLockSession(clockMillis = { now })

        session.markUnlocked()
        session.onBackground(
            isChangingConfigurations = true,
            passwordLockEnabled = true,
        )
        now += AutoLockTimeout.FIVE_MINUTES.timeoutMillis + 1
        session.onForeground(
            timeout = AutoLockTimeout.FIVE_MINUTES,
            passwordLockEnabled = true,
        )

        assertTrue(session.isUnlocked.value)
    }

    @Test
    fun foregroundAfterTimeoutLocksSession() {
        var now = 1_000L
        val session = AppLockSession(clockMillis = { now })

        session.markUnlocked()
        session.onBackground(
            isChangingConfigurations = false,
            passwordLockEnabled = true,
        )
        now += AutoLockTimeout.FIVE_MINUTES.timeoutMillis
        session.onForeground(
            timeout = AutoLockTimeout.FIVE_MINUTES,
            passwordLockEnabled = true,
        )

        assertFalse(session.isUnlocked.value)
    }

    @Test
    fun foregroundBeforeTimeoutKeepsSessionUnlocked() {
        var now = 1_000L
        val session = AppLockSession(clockMillis = { now })

        session.markUnlocked()
        session.onBackground(
            isChangingConfigurations = false,
            passwordLockEnabled = true,
        )
        now += AutoLockTimeout.FIVE_MINUTES.timeoutMillis - 1
        session.onForeground(
            timeout = AutoLockTimeout.FIVE_MINUTES,
            passwordLockEnabled = true,
        )

        assertTrue(session.isUnlocked.value)
    }
}
