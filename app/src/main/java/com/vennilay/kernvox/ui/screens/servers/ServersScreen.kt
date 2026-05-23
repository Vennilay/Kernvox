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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.HubOverview
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.EmptyState
import com.vennilay.kernvox.ui.components.ErrorContent
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
    val isPasswordLockEnabled by viewModel.isPasswordLockEnabled.collectAsState()

    ServersScreenContent(
        uiState = uiState,
        hubOverview = hubOverview,
        isRefreshing = isRefreshing,
        isPasswordLockEnabled = isPasswordLockEnabled,
        onNavigateToSettings = onNavigateToSettings,
        onServerClick = onServerClick,
        onRefresh = { viewModel.loadServers() },
        onRetry = { viewModel.loadServers() },
        onLockClick = viewModel::lockApp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServersScreenContent(
    uiState: UiState<List<Server>>,
    hubOverview: HubOverview?,
    isRefreshing: Boolean,
    isPasswordLockEnabled: Boolean,
    onNavigateToSettings: () -> Unit,
    onServerClick: (Server) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                hubOverview = hubOverview,
                isPasswordLockEnabled = isPasswordLockEnabled,
                onLockClick = onLockClick,
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

                is UiState.Error -> {
                    val isNotConfigured =
                        state.message.resId == R.string.error_missing_connection_settings
                    ErrorContent(
                        title = stringResource(
                            if (isNotConfigured) R.string.servers_not_configured_title
                            else R.string.servers_error_title,
                        ),
                        message = state.message.asString(),
                        retryLabel = stringResource(
                            if (isNotConfigured) R.string.servers_open_settings
                            else R.string.servers_retry,
                        ),
                        onRetry = if (isNotConfigured) onNavigateToSettings else onRetry,
                        paddingValues = paddingValues,
                    )
                }

                is UiState.Success -> {
                    val servers = state.data
                    val locale = LocalLocale.current.platformLocale
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        state = rememberPullToRefreshState(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.md)
                                .semantics { testTag = "servers_list" },
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = Spacing.sm),
                        ) {
                            item(contentType = "nodes_header") {
                                SectionHeader(
                                    label = stringResource(R.string.servers_nodes_subtitle, servers.size)
                                        .uppercase(locale),
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
    hubOverview: HubOverview?,
    isPasswordLockEnabled: Boolean,
    onLockClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_server_placeholder),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
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
                if (hubOverview != null) {
                    HubSubtitle(hubOverview = hubOverview)
                }
            }
        },
        actions = {
            if (isPasswordLockEnabled) {
                IconButton(onClick = onLockClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = stringResource(R.string.servers_lock_cd),
                    )
                }
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.semantics { testTag = "settings_button" },
            ) {
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
private fun HubSubtitle(hubOverview: HubOverview) {
    val lastSync = remember(hubOverview.lastUpdate) {
        hubOverview.lastUpdate?.let { formatTimestamp(it) }
    }
    val onlineSummary = stringResource(R.string.servers_hub_online_summary, hubOverview.totalNodes)
    val syncSummary = lastSync?.let { stringResource(R.string.servers_hub_sync_time, it) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val text = remember(hubOverview.availableNodes, onlineSummary, syncSummary, primaryColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                append(hubOverview.availableNodes.toString())
            }
            append(onlineSummary)
            if (syncSummary != null) {
                append(syncSummary)
            }
        }
    }
    Text(
        modifier = Modifier.padding(start = 40.dp + Spacing.sm),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.xs),
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
