package com.vennilay.kernvox

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vennilay.kernvox.auth.AppLockSessionHolder
import com.vennilay.kernvox.auth.BiometricAuth
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.data.storage.AutoLockTimeout
import com.vennilay.kernvox.data.storage.ThemeMode
import com.vennilay.kernvox.ui.screens.home.HomeScreen
import com.vennilay.kernvox.ui.screens.lock.LockScreen
import com.vennilay.kernvox.ui.screens.serverDetail.ServerDetailScreen
import com.vennilay.kernvox.ui.screens.servers.ServersScreen
import com.vennilay.kernvox.ui.screens.settings.SettingsScreen
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import kotlinx.coroutines.launch

/**
 * Главная активность приложения Kernvox.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(resolveBootstrapBackgroundColor()))
        enableEdgeToEdge()
        setContent {
            AppRoot(activity = this)
        }
    }
}

@Composable
private fun AppRoot(activity: FragmentActivity) {
    val context = LocalContext.current.applicationContext
    val settingsRepository = remember(context) { AppSettingsRepository(context) }
    val settings by settingsRepository.settings.collectAsState(initial = null)
    val currentSettings = settings
    val bootstrapSettings = currentSettings?.toBootstrapSettings()
        ?: AppBootstrapSettingsHolder.lastSettings
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (bootstrapSettings?.themeMode ?: ThemeMode.SYSTEM) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val lockSession = remember { AppLockSessionHolder.session }
    val unlocked by lockSession.isUnlocked.collectAsState()

    LaunchedEffect(bootstrapSettings?.isPasswordLockEnabled) {
        if (bootstrapSettings?.isPasswordLockEnabled == false) {
            lockSession.onPasswordLockDisabled()
        }
    }

    SideEffect {
        if (currentSettings != null) {
            AppBootstrapSettingsHolder.lastSettings = currentSettings.toBootstrapSettings()
        }
    }

    DisposableEffect(
        activity,
        bootstrapSettings?.isPasswordLockEnabled,
        bootstrapSettings?.autoLockTimeout,
    ) {
        val settingsSnapshot = bootstrapSettings
        val observer = LifecycleEventObserver { _, event ->
            if (settingsSnapshot == null) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    lockSession.onBackground(
                        isChangingConfigurations = activity.isChangingConfigurations,
                        passwordLockEnabled = settingsSnapshot.isPasswordLockEnabled,
                    )
                }

                Lifecycle.Event.ON_START -> {
                    lockSession.onForeground(
                        timeout = settingsSnapshot.autoLockTimeout,
                        passwordLockEnabled = settingsSnapshot.isPasswordLockEnabled,
                    )
                }

                else -> Unit
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose {
            activity.lifecycle.removeObserver(observer)
        }
    }

    KernvoxTheme(darkTheme = darkTheme) {
        when {
            bootstrapSettings == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            bootstrapSettings.isPasswordLockEnabled && !unlocked -> {
                LockScreen(
                    biometricEnabled = bootstrapSettings.isBiometricUnlockEnabled,
                    biometricAvailable = BiometricAuth.isAvailable(context),
                    onUnlocked = { lockSession.markUnlocked() },
                    onBiometricClick = { onSuccess, onError ->
                        BiometricAuth.showPrompt(
                            activity = activity,
                            onSuccess = onSuccess,
                            onError = onError,
                        )
                    },
                )
            }

            else -> {
                AppNavHost(
                    hasSeenWelcome = bootstrapSettings.hasSeenWelcome,
                    settingsRepository = settingsRepository,
                )
            }
        }
    }
}

/**
 * Навигационный граф приложения.
 */
@Composable
private fun AppNavHost(
    hasSeenWelcome: Boolean,
    settingsRepository: AppSettingsRepository,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val startDestination = remember(hasSeenWelcome) {
        if (hasSeenWelcome) Screen.Servers.route else Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenApp = {
                    coroutineScope.launch {
                        runCatching { settingsRepository.markWelcomeSeen() }
                        navController.navigate(Screen.Servers.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(Screen.Servers.route) {
            ServersScreen(
                onServerClick = { server ->
                    navController.navigate(Screen.Detail.createRoute(server.id)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("serverId") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getInt("serverId") ?: return@composable
            ServerDetailScreen(
                serverId = serverId,
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}

/**
 * Экраны приложения с маршрутами для Navigation Compose.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Servers : Screen("servers")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{serverId}") {
        fun createRoute(serverId: Int) = "detail/$serverId"
    }
}

private data class AppBootstrapSettings(
    val hasSeenWelcome: Boolean,
    val themeMode: ThemeMode,
    val autoLockTimeout: AutoLockTimeout,
    val isPasswordLockEnabled: Boolean,
    val isBiometricUnlockEnabled: Boolean,
)

private object AppBootstrapSettingsHolder {
    var lastSettings: AppBootstrapSettings? = null
}

private fun AppSettings.toBootstrapSettings(): AppBootstrapSettings =
    AppBootstrapSettings(
        hasSeenWelcome = hasSeenWelcome,
        themeMode = themeMode,
        autoLockTimeout = autoLockTimeout,
        isPasswordLockEnabled = isPasswordLockEnabled,
        isBiometricUnlockEnabled = isBiometricUnlockEnabled,
    )

private fun MainActivity.resolveBootstrapBackgroundColor(): Int {
    val darkTheme = when (AppBootstrapSettingsHolder.lastSettings?.themeMode ?: ThemeMode.SYSTEM) {
        ThemeMode.SYSTEM -> {
            val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    return if (darkTheme) {
        Color.rgb(15, 23, 42)
    } else {
        Color.rgb(248, 250, 252)
    }
}
