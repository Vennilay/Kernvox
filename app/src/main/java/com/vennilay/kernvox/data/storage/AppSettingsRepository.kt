package com.vennilay.kernvox.data.storage

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AutoLockTimeout(
    val storageValue: String,
    val timeoutMillis: Long,
) {
    IMMEDIATE("immediate", 0L),
    FIVE_MINUTES("five_minutes", 5 * 60 * 1000L),
    TEN_MINUTES("ten_minutes", 10 * 60 * 1000L),
    THIRTY_MINUTES("thirty_minutes", 30 * 60 * 1000L);

    fun shouldLockAfter(elapsedMillis: Long): Boolean = elapsedMillis >= timeoutMillis

    companion object {
        fun fromStorageValue(value: String?): AutoLockTimeout =
            entries.firstOrNull { it.storageValue == value } ?: FIVE_MINUTES
    }
}

data class AppSettings(
    val serverUrl: String = "",
    val apiKey: String = "",
    val actionKey: String = "",
    val hasSeenWelcome: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.FIVE_MINUTES,
    val isPasswordLockEnabled: Boolean = false,
    val isBiometricUnlockEnabled: Boolean = false,
)

/**
 * Сохраняет настройки пользователя и учетные данные для блокировки приложения в хранилище данных Preferences DataStore.
 *
 * Пароль приложения хранится в виде хеша PBKDF2 с добавлением случайного salt значения, но никогда в виде открытого текста. Это обеспечивает защиту
 * от случайного просмотра на локальном устройстве, при этом реализация остается независимой от конкретных библиотек.
 */
class AppSettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            serverUrl = prefs[KEY_SERVER_URL] ?: "",
            apiKey = prefs[KEY_API_KEY] ?: "",
            actionKey = prefs[KEY_ACTION_KEY] ?: "",
            hasSeenWelcome = prefs[KEY_HAS_SEEN_WELCOME] ?: false,
            themeMode = prefs[KEY_THEME_MODE]?.toThemeMode() ?: ThemeMode.SYSTEM,
            autoLockTimeout = AutoLockTimeout.fromStorageValue(prefs[KEY_AUTO_LOCK_TIMEOUT]),
            isPasswordLockEnabled = prefs[KEY_PASSWORD_HASH] != null,
            isBiometricUnlockEnabled = prefs[KEY_BIOMETRIC_UNLOCK_ENABLED] ?: false,
        )
    }

    suspend fun saveSettings(serverUrl: String, apiKey: String, actionKey: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = serverUrl
            prefs[KEY_API_KEY] = apiKey
            prefs[KEY_ACTION_KEY] = actionKey
        }
    }

    suspend fun markWelcomeSeen() {
        dataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_WELCOME] = true
        }
    }

    suspend fun resetWelcome() {
        dataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_WELCOME] = false
        }
    }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = themeMode.name
        }
    }

    suspend fun saveAutoLockTimeout(timeout: AutoLockTimeout) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTO_LOCK_TIMEOUT] = timeout.storageValue
        }
    }

    suspend fun setPassword(password: String) {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        dataStore.edit { prefs ->
            prefs[KEY_PASSWORD_SALT] = salt
            prefs[KEY_PASSWORD_HASH] = hash
        }
    }

    suspend fun clearPassword() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PASSWORD_SALT)
            prefs.remove(KEY_PASSWORD_HASH)
            prefs[KEY_BIOMETRIC_UNLOCK_ENABLED] = false
        }
    }

    suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_UNLOCK_ENABLED] = enabled
        }
    }

    suspend fun verifyPassword(password: String): Boolean {
        val prefs = dataStore.data.first()
        val currentSalt = prefs[KEY_PASSWORD_SALT] ?: return false
        val currentHash = prefs[KEY_PASSWORD_HASH] ?: return false
        return MessageDigest.isEqual(
            hashPassword(password, currentSalt).toByteArray(),
            currentHash.toByteArray(),
        )
    }

    companion object {
        private const val HASH_ITERATIONS = 120_000
        private const val HASH_KEY_LENGTH = 256
        private const val SALT_LENGTH_BYTES = 16

        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_ACTION_KEY = stringPreferencesKey("action_key")
        private val KEY_HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_AUTO_LOCK_TIMEOUT = stringPreferencesKey("auto_lock_timeout")
        private val KEY_PASSWORD_SALT = stringPreferencesKey("password_salt")
        private val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        private val KEY_BIOMETRIC_UNLOCK_ENABLED = booleanPreferencesKey("biometric_unlock_enabled")

        private fun generateSalt(): String {
            val bytes = ByteArray(SALT_LENGTH_BYTES)
            SecureRandom().nextBytes(bytes)
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        private fun hashPassword(password: String, salt: String): String {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            val spec = PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                HASH_ITERATIONS,
                HASH_KEY_LENGTH,
            )
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            return Base64.encodeToString(factory.generateSecret(spec).encoded, Base64.NO_WRAP)
        }

        private fun String.toThemeMode(): ThemeMode =
            runCatching { ThemeMode.valueOf(this) }.getOrDefault(ThemeMode.SYSTEM)
    }
}
