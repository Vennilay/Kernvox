package com.vennilay.kernvox.ui.screens.lock

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vennilay.kernvox.R
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.viewmodel.LockViewModel
import com.vennilay.kernvox.viewmodel.LockViewModelFactory

/**
 * App-lock entry screen shown before navigation when local password protection is enabled.
 */
@Composable
fun LockScreen(
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    onUnlocked: () -> Unit,
    onBiometricClick: (onSuccess: () -> Unit, onError: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockViewModel = viewModel(
        factory = LockViewModelFactory(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    var password by rememberSaveable { mutableStateOf("") }
    val canUseBiometric = biometricEnabled && biometricAvailable

    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) {
            onBiometricClick(onUnlocked, viewModel::showBiometricError)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconCircle(
                        icon = R.drawable.ic_settings,
                        containerSize = 56,
                        iconSize = 28,
                        rounded = true,
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(R.string.lock_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.lock_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.lock_password_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = uiState.error != null,
                        supportingText = uiState.error?.let { error ->
                            { Text(error.asString()) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    KernvoxButton(
                        onClick = { viewModel.unlockWithPassword(password, onUnlocked) },
                        enabled = !uiState.isChecking,
                    ) {
                        Text(stringResource(R.string.lock_unlock_button))
                    }
                    if (canUseBiometric) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        OutlinedButton(
                            onClick = { onBiometricClick(onUnlocked, viewModel::showBiometricError) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.lock_biometric_button))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
