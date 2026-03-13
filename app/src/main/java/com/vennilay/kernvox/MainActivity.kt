package com.vennilay.kernvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import com.vennilay.kernvox.viewmodel.ServersViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

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
    var showServers by rememberSaveable { mutableStateOf(false) }

    if (!showServers) {
        HomeScreen(
            onOpenApp = { showServers = true }
        )
    } else {
        ServersScreen(
            onNavigateBack = { showServers = false },
            onAddServer = { }
        )
    }
}

@Composable
private fun HomeScreen(
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.home_welcome_title),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(R.string.home_description_line_1))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = stringResource(R.string.home_description_line_2))
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = onOpenApp) {
                    Text(text = stringResource(R.string.home_open_app_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServersScreen(
    onNavigateBack: () -> Unit,
    onAddServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serversViewModel: ServersViewModel = viewModel()
    val servers by serversViewModel.servers.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addServerSoonMessage = stringResource(R.string.servers_add_soon_snackbar)

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.servers_title)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = servers,
                        key = { it.id },
                    ) { server ->
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = server.name, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "${server.host}:${server.port}")
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = "Статус")
                                    Text(text = if (server.isAvailable) "Онлайн" else "Оффлайн")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        onAddServer()
                        scope.launch {
                            snackbarHostState.showSnackbar(addServerSoonMessage)
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.servers_add_button))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KernvoxTheme {
        HomeScreen(onOpenApp = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ServersScreenPreview() {
    KernvoxTheme {
        ServersScreen(onNavigateBack = {}, onAddServer = {})
    }
}