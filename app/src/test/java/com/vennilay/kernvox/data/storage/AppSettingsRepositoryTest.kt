package com.vennilay.kernvox.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AppSettingsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val scopes = mutableListOf<CoroutineScope>()

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
    }

    @Test
    fun migratesPlaintextSecretsToEncryptedPreferences() = runBlocking {
        val dataStore = createDataStore()
        val secretStorage = testSecretStorage()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("api_key")] = "legacy-api-key"
            prefs[stringPreferencesKey("action_key")] = "legacy-action-key"
        }

        val repository = AppSettingsRepository(dataStore, secretStorage)
        val settings = repository.settings.first()
        val migratedPrefs = dataStore.data.first()
        val encryptedApiKey = migratedPrefs[stringPreferencesKey("encrypted_api_key")]
        val encryptedActionKey = migratedPrefs[stringPreferencesKey("encrypted_action_key")]

        assertEquals("legacy-api-key", settings.apiKey)
        assertEquals("legacy-action-key", settings.actionKey)
        assertNull(migratedPrefs[stringPreferencesKey("api_key")])
        assertNull(migratedPrefs[stringPreferencesKey("action_key")])
        assertNotNull(encryptedApiKey)
        assertNotNull(encryptedActionKey)
        assertNotEquals("legacy-api-key", encryptedApiKey)
        assertNotEquals("legacy-action-key", encryptedActionKey)
        assertEquals("legacy-api-key", secretStorage.decryptString(encryptedApiKey!!))
        assertEquals("legacy-action-key", secretStorage.decryptString(encryptedActionKey!!))
    }

    @Test
    fun saveSettingsWritesEncryptedSecretsAndRemovesLegacyPreferences() = runBlocking {
        val dataStore = createDataStore()
        val secretStorage = testSecretStorage()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("api_key")] = "old-api-key"
            prefs[stringPreferencesKey("action_key")] = "old-action-key"
        }

        val repository = AppSettingsRepository(dataStore, secretStorage)
        repository.saveSettings(
            serverUrl = "https://kernvox.example.com",
            apiKey = "new-api-key",
            actionKey = "new-action-key",
        )
        val prefs = dataStore.data.first()
        val encryptedApiKey = prefs[stringPreferencesKey("encrypted_api_key")]
        val encryptedActionKey = prefs[stringPreferencesKey("encrypted_action_key")]

        assertEquals("https://kernvox.example.com", prefs[stringPreferencesKey("server_url")])
        assertNull(prefs[stringPreferencesKey("api_key")])
        assertNull(prefs[stringPreferencesKey("action_key")])
        assertNotNull(encryptedApiKey)
        assertNotNull(encryptedActionKey)
        assertEquals("new-api-key", secretStorage.decryptString(encryptedApiKey!!))
        assertEquals("new-action-key", secretStorage.decryptString(encryptedActionKey!!))
    }

    private fun createDataStore(): DataStore<Preferences> {
        val file = File(
            temporaryFolder.newFolder(),
            "settings.preferences_pb",
        )
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scopes += scope
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
    }

    private fun testSecretStorage(): SecureSecretStorage =
        SecureSecretStorage(
            cipher = JvmAesGcmSecretCipher(),
            base64Codec = JvmBase64Codec,
        )

    private object JvmBase64Codec : Base64Codec {
        override fun encode(bytes: ByteArray): String =
            Base64.getEncoder().encodeToString(bytes)

        override fun decode(value: String): ByteArray =
            Base64.getDecoder().decode(value)
    }

    private class JvmAesGcmSecretCipher : SecretCipher {
        private val key: SecretKey = KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()

        override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return EncryptedSecretPayload(
                iv = cipher.iv,
                ciphertext = cipher.doFinal(plaintext),
            )
        }

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, payload.iv))
            return cipher.doFinal(payload.ciphertext)
        }
    }
}
