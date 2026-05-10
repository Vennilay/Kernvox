package com.vennilay.kernvox.auth

import com.vennilay.kernvox.data.storage.AutoLockTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит только несекретное состояние текущей разблокированной сессии приложения.
 *
 * Пароль, хеш, salt и другие секреты здесь не сохраняются. Сессия живет в процессе приложения, поэтому переживает
 * configuration change, но сбрасывается после смерти процесса.
 */
class AppLockSession(
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var backgroundedAtMillis: Long? = null
    private var lastUnlockedAtMillis: Long? = null

    fun markUnlocked() {
        _isUnlocked.value = true
        lastUnlockedAtMillis = clockMillis()
        backgroundedAtMillis = null
    }

    fun markLocked() {
        _isUnlocked.value = false
        backgroundedAtMillis = null
    }

    fun onPasswordLockDisabled() {
        markUnlocked()
    }

    fun onBackground(
        isChangingConfigurations: Boolean,
        passwordLockEnabled: Boolean,
    ) {
        if (!isChangingConfigurations && passwordLockEnabled && _isUnlocked.value) {
            backgroundedAtMillis = clockMillis()
        }
    }

    fun onForeground(
        timeout: AutoLockTimeout,
        passwordLockEnabled: Boolean,
    ) {
        if (!passwordLockEnabled) {
            onPasswordLockDisabled()
            return
        }

        val backgroundedAt = backgroundedAtMillis ?: return
        val unlockedAt = lastUnlockedAtMillis
        if (unlockedAt != null && backgroundedAt < unlockedAt) {
            backgroundedAtMillis = null
            return
        }
        val elapsedMillis = (clockMillis() - backgroundedAt).coerceAtLeast(0L)
        if (timeout.shouldLockAfter(elapsedMillis)) {
            markLocked()
        } else {
            backgroundedAtMillis = null
        }
    }
}

object AppLockSessionHolder {
    val session = AppLockSession()
}
