package com.vennilay.kernvox.ui.screens.servers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.vennilay.kernvox.data.model.HubOverview
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.EmptyState
import com.vennilay.kernvox.ui.components.ErrorContent
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.LoadingContent
import com.vennilay.kernvox.ui.components.ServerCard
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.ui.utils.formatTimestamp
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
    val hubOverview by viewModel.hubOverview.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ServersTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "servers_main_state",
        ) { state ->
            when (state) {
                is UiState.Loading -> LoadingContent(paddingValues = paddingValues)

                is UiState.Error -> ErrorContent(
                    title = stringResource(R.string.servers_error_title),
                    message = state.message,
                    retryLabel = stringResource(R.string.servers_retry),
                    onRetry = { viewModel.loadServers() },
                    paddingValues = paddingValues,
                )

                is UiState.Success -> {
                    val servers = state.data
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadServers() },
                        state = rememberPullToRefreshState(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(vertical = Spacing.sm),
                        ) {
                            hubOverview?.let { overview ->
                                item(contentType = "hub_card") {
                                    HubOverviewCard(hubOverview = overview)
                                }
                            }

                            item(contentType = "nodes_header") {
                                SectionHeader(
                                    title = stringResource(R.string.servers_nodes_title),
                                    subtitle = stringResource(
                                        R.string.servers_nodes_subtitle,
                                        servers.size
                                    ),
                                )
                            }

                            if (servers.isEmpty()) {
                                item(contentType = "empty_state") {
                                    EmptyState(
                                        title = stringResource(R.string.servers_empty_title),
                                        subtitle = stringResource(R.string.servers_no_results_subtitle),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServersTopAppBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onNavigateToSettings: () -> Unit,
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
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(R.string.servers_hub_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.servers_settings_cd),
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
private fun HubOverviewCard(
    hubOverview: HubOverview,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                .padding(Spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                IconCircle(
                    icon = R.drawable.ic_server_placeholder,
                    containerSize = 44,
                    iconSize = 22,
                    rounded = true,
                )
                Column {
                    Text(
                        text = hubOverview.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.servers_hub_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        InfoRow(
            label = stringResource(R.string.servers_hub_url_label),
            value = hubOverview.baseUrl,
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        InfoRow(
            label = stringResource(R.string.servers_hub_host_label),
            value = hubOverview.port?.let { "${hubOverview.host}:$it" } ?: hubOverview.host,
        )
        Spacer(modifier = Modifier.height(Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HubMetric(
                label = stringResource(R.string.servers_hub_total_nodes),
                value = hubOverview.totalNodes.toString(),
            )
            HubMetric(
                label = stringResource(R.string.servers_hub_available_nodes),
                value = hubOverview.availableNodes.toString(),
            )
            HubMetric(
                label = stringResource(R.string.servers_hub_last_sync),
                value = hubOverview.lastUpdate?.let(::formatTimestamp)
                    ?: stringResource(R.string.server_card_no_data),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HubMetric(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
