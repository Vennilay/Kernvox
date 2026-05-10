package com.vennilay.kernvox.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.vennilay.kernvox.R

/**
 * Маленький обёрточный модуль для AndroidX Biometric, используемый в экранах блокировки приложений и настройках
 */
object BiometricAuth {
    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG

    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun showPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (cancelled: Boolean) -> Unit,
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON)
                }

                override fun onAuthenticationFailed() {
                    onError(false)
                }
            },
        )

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.lock_biometric_title))
                .setSubtitle(activity.getString(R.string.lock_biometric_subtitle))
                .setNegativeButtonText(activity.getString(R.string.lock_biometric_negative))
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build(),
        )
    }
}
