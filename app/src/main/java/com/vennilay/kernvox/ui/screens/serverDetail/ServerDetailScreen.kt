package com.vennilay.kernvox.ui.screens.serverDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.InfoTile
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.components.StatusBadge
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.utils.formatTimestamp
import com.vennilay.kernvox.ui.utils.formatUptime
import com.vennilay.kernvox.viewmodel.DetailViewModel
import com.vennilay.kernvox.viewmodel.DetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    serverId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DetailViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
            serverId,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(onBack = onNavigateBack)

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ServerDetailTopAppBar(
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                onRefresh = { viewModel.refresh() },
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
                        text = stringResource(R.string.server_detail_loading_error),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    KernvoxButton(onClick = { viewModel.refresh() }) {
                        Text(stringResource(R.string.server_detail_retry))
                    }
                }
            }

            is UiState.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                ServerDetailContent(server = (state as UiState.Success<Server>).data)
            }
        }
    }
}

@Composable
private fun ServerDetailContent(server: Server) {
    val ramUnit = stringResource(R.string.server_detail_ram_unit)
    val daysUnit = stringResource(R.string.time_days)
    val hoursUnit = stringResource(R.string.time_hours)
    val minutesUnit = stringResource(R.string.time_minutes)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ServerHeader(server = server)
        Spacer(modifier = Modifier.height(24.dp))

        InfoTile(
            label = stringResource(R.string.server_detail_address),
            value = "${server.host}:${server.port}",
            icon = R.drawable.ic_location,
        )
        Spacer(modifier = Modifier.height(12.dp))

        server.username?.let { username ->
            InfoTile(
                label = stringResource(R.string.server_detail_username),
                value = username,
                icon = R.drawable.ic_server_placeholder,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        server.cpuPercent?.let { cpu ->
            InfoTile(
                label = stringResource(R.string.server_detail_cpu),
                value = "%.1f%%".format(cpu),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (server.ramUsedMb != null && server.ramTotalMb != null) {
            InfoTile(
                label = stringResource(R.string.server_detail_ram),
                value = "%.0f $ramUnit / %.0f $ramUnit (%.1f%%)".format(
                    server.ramUsedMb,
                    server.ramTotalMb,
                    server.ramPercent ?: 0f,
                ),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        server.diskUsedPercent?.let { disk ->
            InfoTile(
                label = stringResource(R.string.server_detail_disk),
                value = "%.1f%%".format(disk),
                icon = R.drawable.ic_server_placeholder,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        server.uptimeFormatted?.let { uptime ->
            InfoTile(
                label = stringResource(R.string.server_detail_uptime),
                value = uptime,
                icon = R.drawable.ic_uptime,
            )
        } ?: server.uptimeSeconds?.let { seconds ->
            InfoTile(
                label = stringResource(R.string.server_detail_uptime),
                value = formatUptime(seconds.toLong(), daysUnit, hoursUnit, minutesUnit),
                icon = R.drawable.ic_uptime,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        server.lastMetricTimestamp?.let { ts ->
            InfoTile(
                label = stringResource(R.string.server_detail_last_metric),
                value = formatTimestamp(ts),
                icon = R.drawable.ic_quickview,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerDetailTopAppBar(
    onNavigateBack: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            Row(
                modifier = Modifier.clickable(onClick = onNavigateBack).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.back_button),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.server_detail_refresh_cd),
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun ServerHeader(server: Server) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconCircle(
            icon = R.drawable.ic_server_placeholder,
            containerSize = 80,
            iconSize = 40,
            rounded = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = server.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(isOnline = server.isAvailable ?: false)
    }
}
