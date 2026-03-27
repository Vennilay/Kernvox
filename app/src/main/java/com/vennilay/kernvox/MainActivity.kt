package com.vennilay.kernvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.vennilay.kernvox.ui.screens.home.HomeScreen
import com.vennilay.kernvox.ui.screens.servers.ServersScreen
import com.vennilay.kernvox.ui.theme.KernvoxTheme

/**
 * Главная активность приложения Kernvox.
 * Управляет навигацией между экранами приветствия и серверов.
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

/**
 * Корневой компонент приложения, управляющий навигацией между экранами.
 */
@Composable
private fun AppRoot() {
    val showServers = rememberSaveable { mutableStateOf(false) }

    if (!showServers.value) {
        HomeScreen(
            onOpenApp = { showServers.value = true }
        )
    } else {
        ServersScreen(
            onNavigateBack = { showServers.value = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppRootPreview() {
    KernvoxTheme {
        AppRoot()
    }
}
