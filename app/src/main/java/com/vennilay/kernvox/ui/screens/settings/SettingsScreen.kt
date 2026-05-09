package com.vennilay.kernvox.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vennilay.kernvox.R
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.theme.GreenSuccess
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.viewmodel.SettingsViewModel
import com.vennilay.kernvox.viewmodel.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
        ),
    ),
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var serverUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var actionKey by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf(false) }

    val isSaving = settingsState is UiState.Loading

    LaunchedEffect(settingsState) {
        if (settingsState is UiState.Success && serverUrl.isEmpty() && apiKey.isEmpty() && actionKey.isEmpty()) {
            val settings = (settingsState as UiState.Success).data
            serverUrl = settings.serverUrl
            apiKey = settings.apiKey
            actionKey = settings.actionKey
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.settings_back_cd),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = stringResource(R.string.settings_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.settings_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = serverUrl,
                onValueChange = {
                    serverUrl = it
                    urlError = false
                },
                label = { Text(stringResource(R.string.settings_server_url_label)) },
                placeholder = { Text(stringResource(R.string.settings_server_url_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = urlError,
                supportingText = if (urlError) {
                    { Text(stringResource(R.string.settings_url_error)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(stringResource(R.string.settings_api_key_label)) },
                placeholder = { Text(stringResource(R.string.settings_api_key_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = actionKey,
                onValueChange = { actionKey = it },
                label = { Text(stringResource(R.string.settings_action_key_label)) },
                placeholder = { Text(stringResource(R.string.settings_action_key_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.settings_action_key_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            AnimatedVisibility(visible = isSaving) {
                Column {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }

            KernvoxButton(
                onClick = {
                    val url = serverUrl.trim()
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        urlError = true
                    } else {
                        urlError = false
                        viewModel.saveSettings(url, apiKey.trim(), actionKey.trim())
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_save_button))
            }

            when (settingsState) {
                is UiState.Success -> {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(R.string.settings_saved_ok),
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenSuccess,
                    )
                }

                is UiState.Error -> {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = (settingsState as UiState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                else -> Unit
            }
        }
    }
}
