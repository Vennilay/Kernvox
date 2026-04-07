package com.vennilay.kernvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vennilay.kernvox.ui.screens.home.HomeScreen
import com.vennilay.kernvox.ui.screens.serverDetail.ServerDetailScreen
import com.vennilay.kernvox.ui.screens.servers.ServersScreen
import com.vennilay.kernvox.ui.screens.settings.SettingsScreen
import com.vennilay.kernvox.ui.theme.KernvoxTheme

/**
 * Главная активность приложения Kernvox.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KernvoxTheme {
                AppNavHost()
            }
        }
    }
}

/**
 * Навигационный граф приложения.
 */
@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenApp = {
                    navController.navigate(Screen.Servers.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Servers.route) {
            ServersScreen(
                onServerClick = { server ->
                    navController.navigate(Screen.Detail.createRoute(server.id))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }

        composable(
            route = Screen.Detail.routeWithArgs,
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
        const val routeWithArgs = "detail/{serverId}"
    }
}
