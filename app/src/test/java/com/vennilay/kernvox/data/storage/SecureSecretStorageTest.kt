package com.vennilay.kernvox.data.storage

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SecureSecretStorageTest {

    @Test
    fun encryptDecryptReturnsOriginalValue() {
        val storage = testSecretStorage()

        val encrypted = storage.encryptString("secret-value")

        assertNotEquals("secret-value", encrypted)
        assertEquals("secret-value", storage.decryptString(encrypted))
    }

    @Test
    fun repeatedEncryptionUsesDifferentIv() {
        val storage = testSecretStorage()

        val first = storage.encryptString("same-secret")
        val second = storage.encryptString("same-secret")

        assertNotEquals(first, second)
        assertEquals("same-secret", storage.decryptString(first))
        assertEquals("same-secret", storage.decryptString(second))
    }

    @Test
    fun emptyStringStaysEmpty() {
        val storage = testSecretStorage()

        assertEquals("", storage.encryptString(""))
        assertEquals("", storage.decryptString(""))
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
