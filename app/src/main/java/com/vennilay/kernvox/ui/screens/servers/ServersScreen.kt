package com.vennilay.kernvox.ui.screens.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.EmptyState
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.components.ServerCard
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.viewmodel.ServersViewModel
import com.vennilay.kernvox.viewmodel.ServersViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    onServerClick: (Server) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ServersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ServersViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
        ),
    ),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val servers by viewModel.servers.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.loadServers()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ServersTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateToSettings = onNavigateToSettings,
                onRefresh = { viewModel.loadServers() },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Ошибка",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    KernvoxButton(onClick = { viewModel.loadServers() }) {
                        Text("Повторить")
                    }
                }
            }

            is UiState.Success -> {
                if (servers.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.servers_empty_title),
                        subtitle = "Серверы не найдены. Добавьте серверы через KernvoxHub.",
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        items(
                            items = servers,
                            key = { it.id },
                            contentType = { "server_card" },
                        ) { server ->
                            ServerCard(
                                server = server,
                                onClick = onServerClick,
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServersTopAppBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onNavigateToSettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_server_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.servers_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(painter = painterResource(R.drawable.ic_refresh), contentDescription = "Обновить")
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Настройки")
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
