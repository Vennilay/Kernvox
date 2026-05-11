package com.vennilay.kernvox.data.storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class EncryptedSecretPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray,
)

internal interface SecretCipher {
    fun encrypt(plaintext: ByteArray): EncryptedSecretPayload
    fun decrypt(payload: EncryptedSecretPayload): ByteArray
}

internal interface Base64Codec {
    fun encode(bytes: ByteArray): String
    fun decode(value: String): ByteArray
}

internal object AndroidBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    override fun decode(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP)
}

internal class SecretStorageException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

internal class SecureSecretStorage(
    private val cipher: SecretCipher = AndroidKeystoreSecretCipher(),
    private val base64Codec: Base64Codec = AndroidBase64Codec,
) {
    fun encryptString(value: String): String {
        if (value.isEmpty()) return ""
        val payload = cipher.encrypt(value.toByteArray(StandardCharsets.UTF_8))
        return "${base64Codec.encode(payload.iv)}:${
            base64Codec.encode(payload.ciphertext)
        }"
    }

    fun decryptString(storedValue: String): String {
        if (storedValue.isEmpty()) return ""
        val parts = storedValue.split(':', limit = 2)
        if (parts.size != ENCRYPTED_PARTS || parts.any { it.isBlank() }) {
            throw SecretStorageException("Encrypted secret has an unsupported format.")
        }

        val payload = try {
            EncryptedSecretPayload(
                iv = base64Codec.decode(parts[0]),
                ciphertext = base64Codec.decode(parts[1]),
            )
        } catch (e: IllegalArgumentException) {
            throw SecretStorageException("Encrypted secret is not valid Base64.", e)
        }

        return try {
            String(cipher.decrypt(payload), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw SecretStorageException("Encrypted secret could not be decrypted.", e)
        }
    }

    private companion object {
        const val ENCRYPTED_PARTS = 2
    }
}

internal class AndroidKeystoreSecretCipher(
    private val keyAlias: String = KEY_ALIAS,
) : SecretCipher {

    override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        return EncryptedSecretPayload(
            iv = cipher.iv,
            ciphertext = cipher.doFinal(plaintext),
        )
    }

    override fun decrypt(payload: EncryptedSecretPayload): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload.iv),
        )
        return cipher.doFinal(payload.ciphertext)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE,
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AES_KEY_SIZE_BITS)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_ALIAS = "kernvox_android_secret_storage"
        const val AES_KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
