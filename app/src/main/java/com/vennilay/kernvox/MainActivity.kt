package com.vennilay.kernvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vennilay.kernvox.data.storage.AppSettings
import com.vennilay.kernvox.data.storage.AppSettingsRepository
import com.vennilay.kernvox.ui.screens.home.HomeScreen
import com.vennilay.kernvox.ui.screens.serverDetail.ServerDetailScreen
import com.vennilay.kernvox.ui.screens.servers.ServersScreen
import com.vennilay.kernvox.ui.screens.settings.SettingsScreen
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import kotlinx.coroutines.launch

/**
 * Главная активность приложения Kernvox.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KernvoxTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current.applicationContext
    val settingsRepository = remember(context) { AppSettingsRepository(context) }
    val settings by settingsRepository.settings.collectAsState(initial = null)

    val currentSettings = settings
    if (currentSettings == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        AppNavHost(
            initialSettings = currentSettings,
            settingsRepository = settingsRepository,
        )
    }
}

/**
 * Навигационный граф приложения.
 */
@Composable
private fun AppNavHost(
    initialSettings: AppSettings,
    settingsRepository: AppSettingsRepository,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val startDestination = remember {
        if (initialSettings.hasSeenWelcome) Screen.Servers.route else Screen.Home.route
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
