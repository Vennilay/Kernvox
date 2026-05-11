package com.vennilay.kernvox.ui.screens.settings

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vennilay.kernvox.BuildConfig
import com.vennilay.kernvox.R
import com.vennilay.kernvox.auth.BiometricAuth
import com.vennilay.kernvox.data.network.ServerUrlValidator
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AutoLockTimeout
import com.vennilay.kernvox.data.storage.ThemeMode
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.viewmodel.SettingsViewModel
import com.vennilay.kernvox.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val settingsState by viewModel.settingsState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val biometricAvailable = remember(context) { BiometricAuth.isAvailable(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var serverUrl by rememberSaveable { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var actionKey by remember { mutableStateOf("") }
    var urlErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordDialog by rememberSaveable { mutableStateOf<PasswordDialogMode?>(null) }

    val settings = (settingsState as? UiState.Success)?.data
    val isSaving = settingsState is UiState.Loading

    LaunchedEffect(settings) {
        if (settings != null && serverUrl.isEmpty() && apiKey.isEmpty() && actionKey.isEmpty()) {
            serverUrl = settings.serverUrl
            apiKey = settings.apiKey
            actionKey = settings.actionKey
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message.resolve(context))
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(Spacing.md))

                AnimatedVisibility(visible = isSaving) {
                    Column {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }
                }

                settings?.let { currentSettings ->
                    AppearanceSection(
                        themeMode = currentSettings.themeMode,
                        onThemeSelected = viewModel::saveThemeMode,
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    SecuritySection(
                        settings = currentSettings,
                        biometricAvailable = biometricAvailable,
                        biometricPromptAvailable = activity != null,
                        onPasswordToggle = {
                            passwordDialog = if (currentSettings.isPasswordLockEnabled) {
                                PasswordDialogMode.Disable
                            } else {
                                PasswordDialogMode.Enable
                            }
                        },
                        onBiometricToggle = { enabled ->
                            if (!enabled) {
                                viewModel.setBiometricUnlockEnabled(false)
                                return@SecuritySection
                            }

                            val promptActivity = activity
                            if (promptActivity == null) {
                                viewModel.notifyBiometricEnableRejected()
                                return@SecuritySection
                            }

                            BiometricAuth.showPrompt(
                                activity = promptActivity,
                                titleRes = R.string.settings_biometric_prompt_title,
                                subtitleRes = R.string.settings_biometric_prompt_subtitle,
                                negativeButtonRes = R.string.settings_biometric_prompt_negative,
                                onSuccess = { viewModel.setBiometricUnlockEnabled(true) },
                                onError = { viewModel.notifyBiometricEnableRejected() },
                            )
                        },
                        onAutoLockSelected = viewModel::saveAutoLockTimeout,
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    AppSection()
                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                ApiSection(
                    serverUrl = serverUrl,
                    apiKey = apiKey,
                    actionKey = actionKey,
                    urlErrorRes = urlErrorRes,
                    isSaving = isSaving,
                    onServerUrlChange = {
                        serverUrl = it
                        urlErrorRes = null
                    },
                    onApiKeyChange = { apiKey = it },
                    onActionKeyChange = { actionKey = it },
                    onSave = {
                        val url = serverUrl.trim()
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            urlErrorRes = R.string.settings_url_error
                        } else if (!ServerUrlValidator.isAllowed(url)) {
                            urlErrorRes = R.string.settings_release_https_error
                        } else {
                            urlErrorRes = null
                            viewModel.saveSettings(url, apiKey.trim(), actionKey.trim())
                        }
                    },
                )

                when (settingsState) {
                    is UiState.Success -> {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = stringResource(R.string.settings_saved_ok),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    is UiState.Error -> {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = (settingsState as UiState.Error).message.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    else -> Unit
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }

    passwordDialog?.let { mode ->
        PasswordDialog(
            mode = mode,
            onDismiss = { passwordDialog = null },
            onEnable = { password ->
                viewModel.enablePassword(password)
                passwordDialog = null
            },
            onDisable = { password, onWrongPassword ->
                viewModel.disablePassword(password) { success ->
                    if (success) {
                        passwordDialog = null
                    } else {
                        onWrongPassword()
                    }
                }
            },
        )
    }
}

@Composable
private fun AppearanceSection(
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        ThemeOption(
            title = stringResource(R.string.settings_theme_system),
            selected = themeMode == ThemeMode.SYSTEM,
            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
        )
        ThemeOption(
            title = stringResource(R.string.settings_theme_light),
            selected = themeMode == ThemeMode.LIGHT,
            onClick = { onThemeSelected(ThemeMode.LIGHT) },
        )
        ThemeOption(
            title = stringResource(R.string.settings_theme_dark),
            selected = themeMode == ThemeMode.DARK,
            onClick = { onThemeSelected(ThemeMode.DARK) },
        )
    }
}

@Composable
private fun SecuritySection(
    settings: AppSettings,
    biometricAvailable: Boolean,
    biometricPromptAvailable: Boolean,
    onPasswordToggle: () -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onAutoLockSelected: (AutoLockTimeout) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_security)) {
        SettingsSwitchRow(
            title = stringResource(R.string.settings_password_lock_title),
            subtitle = stringResource(R.string.settings_password_lock_subtitle),
            checked = settings.isPasswordLockEnabled,
            onCheckedChange = { onPasswordToggle() },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        SettingsSwitchRow(
            title = stringResource(R.string.settings_biometric_title),
            subtitle = when {
                !biometricAvailable || !biometricPromptAvailable -> stringResource(R.string.settings_biometric_unavailable)
                !settings.isPasswordLockEnabled -> stringResource(R.string.settings_biometric_requires_password)
                else -> stringResource(R.string.settings_biometric_subtitle)
            },
            checked = settings.isBiometricUnlockEnabled &&
                biometricAvailable &&
                biometricPromptAvailable &&
                settings.isPasswordLockEnabled,
            enabled = biometricAvailable && biometricPromptAvailable && settings.isPasswordLockEnabled,
            onCheckedChange = onBiometricToggle,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Text(
            text = stringResource(R.string.settings_auto_lock_title),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        Text(
            text = stringResource(R.string.settings_auto_lock_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.xs),
        )
        AutoLockTimeout.entries.forEach { timeout ->
            AutoLockOption(
                timeout = timeout,
                selected = settings.autoLockTimeout == timeout,
                onClick = { onAutoLockSelected(timeout) },
            )
        }
    }
}

@Composable
private fun AppSection() {
    SettingsSection(title = stringResource(R.string.settings_section_app)) {
        SettingsActionRow(
            title = stringResource(R.string.settings_app_version, BuildConfig.VERSION_NAME),
            subtitle = stringResource(R.string.app_name),
            onClick = {},
            enabled = false,
        )
    }
}

@Composable
private fun ApiSection(
    serverUrl: String,
    apiKey: String,
    actionKey: String,
    urlErrorRes: Int?,
    isSaving: Boolean,
    onServerUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onActionKeyChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_api)) {
        val serverUrlSupportingText: (@Composable () -> Unit)? = urlErrorRes?.let { errorRes ->
            { Text(stringResource(errorRes)) }
        }
        Text(
            text = stringResource(R.string.settings_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            label = { Text(stringResource(R.string.settings_server_url_label)) },
            placeholder = { Text(stringResource(R.string.settings_server_url_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus(),
            singleLine = true,
            isError = urlErrorRes != null,
            supportingText = serverUrlSupportingText,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text(stringResource(R.string.settings_api_key_label)) },
            placeholder = { Text(stringResource(R.string.settings_api_key_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        OutlinedTextField(
            value = actionKey,
            onValueChange = onActionKeyChange,
            label = { Text(stringResource(R.string.settings_action_key_label)) },
            placeholder = { Text(stringResource(R.string.settings_action_key_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = stringResource(R.string.settings_action_key_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        KernvoxButton(
            onClick = onSave,
            enabled = !isSaving,
        ) {
            Text(stringResource(R.string.settings_save_button))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(Spacing.md), content = content)
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

@Composable
private fun AutoLockOption(
    timeout: AutoLockTimeout,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = Spacing.sm)) {
            Text(
                text = stringResource(timeout.titleRes),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(timeout.subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PasswordDialog(
    mode: PasswordDialogMode,
    onDismiss: () -> Unit,
    onEnable: (String) -> Unit,
    onDisable: (String, onWrongPassword: () -> Unit) -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var repeatedPassword by rememberSaveable { mutableStateOf("") }
    var errorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    val isEnableMode = mode == PasswordDialogMode.Enable

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (isEnableMode) {
                        R.string.settings_password_dialog_title
                    } else {
                        R.string.settings_password_confirm_title
                    },
                ),
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorRes = null
                    },
                    label = {
                        Text(
                            stringResource(
                                if (isEnableMode) {
                                    R.string.settings_password_label
                                } else {
                                    R.string.settings_current_password_label
                                },
                            ),
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = errorRes != null,
                    modifier = Modifier.bringIntoViewOnFocus(),
                )
                if (isEnableMode) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = repeatedPassword,
                        onValueChange = {
                            repeatedPassword = it
                            errorRes = null
                        },
                        label = { Text(stringResource(R.string.settings_password_repeat_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = errorRes != null,
                        modifier = Modifier.bringIntoViewOnFocus(),
                    )
                }
                errorRes?.let {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        password.length < 4 -> errorRes = R.string.settings_password_too_short
                        isEnableMode && password != repeatedPassword ->
                            errorRes = R.string.settings_password_mismatch

                        isEnableMode -> onEnable(password)
                        else -> onDisable(password) {
                            errorRes = R.string.settings_password_wrong
                        }
                    }
                },
            ) {
                Text(
                    stringResource(
                        if (isEnableMode) {
                            R.string.settings_password_save
                        } else {
                            R.string.settings_password_disable
                        },
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_password_cancel))
            }
        },
    )
}

private val AutoLockTimeout.titleRes: Int
    get() = when (this) {
        AutoLockTimeout.IMMEDIATE -> R.string.settings_auto_lock_immediate_title
        AutoLockTimeout.FIVE_MINUTES -> R.string.settings_auto_lock_5_minutes_title
        AutoLockTimeout.TEN_MINUTES -> R.string.settings_auto_lock_10_minutes_title
        AutoLockTimeout.THIRTY_MINUTES -> R.string.settings_auto_lock_30_minutes_title
    }

private val AutoLockTimeout.subtitleRes: Int
    get() = when (this) {
        AutoLockTimeout.IMMEDIATE -> R.string.settings_auto_lock_immediate_subtitle
        AutoLockTimeout.FIVE_MINUTES -> R.string.settings_auto_lock_5_minutes_subtitle
        AutoLockTimeout.TEN_MINUTES -> R.string.settings_auto_lock_10_minutes_subtitle
        AutoLockTimeout.THIRTY_MINUTES -> R.string.settings_auto_lock_30_minutes_subtitle
    }

private tailrec fun Context.findFragmentActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Modifier.bringIntoViewOnFocus(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused, imeBottom) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    return this.bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
            if (focusState.isFocused) {
                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
            }
        }
}

private enum class PasswordDialogMode {
    Enable,
    Disable,
}
