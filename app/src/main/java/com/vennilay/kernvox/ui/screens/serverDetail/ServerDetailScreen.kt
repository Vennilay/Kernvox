package com.vennilay.kernvox.ui.screens.serverDetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.EmptyState
import com.vennilay.kernvox.ui.components.ErrorContent
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.InfoTile
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.components.LoadingContent
import com.vennilay.kernvox.ui.components.MetricHistoryRow
import com.vennilay.kernvox.ui.components.ProcessItem
import com.vennilay.kernvox.ui.components.StatusBadge
import com.vennilay.kernvox.ui.state.UiState
import com.vennilay.kernvox.ui.theme.Spacing
import com.vennilay.kernvox.ui.utils.formatBytes
import com.vennilay.kernvox.ui.utils.formatTimestamp
import com.vennilay.kernvox.ui.utils.formatUptime
import com.vennilay.kernvox.viewmodel.DetailViewModel
import com.vennilay.kernvox.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.launch

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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val totalProcesses by viewModel.totalProcesses.collectAsState()
    val isRebooting by viewModel.isRebooting.collectAsState()

    BackHandler(onBack = onNavigateBack)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRebootConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ServerDetailTopAppBar(
                serverName = when (val s = uiState) {
                    is UiState.Success -> s.data.name
                    else -> ""
                },
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "detail_main_state",
        ) { state ->
            when (state) {
                is UiState.Loading -> LoadingContent(paddingValues = paddingValues)

                is UiState.Error -> ErrorContent(
                    title = stringResource(R.string.server_detail_loading_error),
                    message = state.message,
                    retryLabel = stringResource(R.string.server_detail_retry),
                    onRetry = { viewModel.refresh() },
                    paddingValues = paddingValues,
                )

                is UiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        state = rememberPullToRefreshState(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val tabTitles = listOf(
                                stringResource(R.string.server_detail_tab_overview),
                                buildString {
                                    append(stringResource(R.string.server_detail_tab_processes))
                                    if (totalProcesses > 0) append(" ($totalProcesses)")
                                },
                                stringResource(R.string.server_detail_tab_history),
                            )

                            PrimaryScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                edgePadding = Spacing.md,
                                divider = {},
                            ) {
                                tabTitles.forEachIndexed { index, title ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            viewModel.onTabSelected(index)
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = { Text(title, maxLines = 1) },
                                    )
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                            ) { page ->
                                when (page) {
                                    0 -> OverviewTab(
                                        server = state.data,
                                        isRebooting = isRebooting,
                                        onRebootClick = { showRebootConfirmation = true },
                                    )
                                    1 -> ProcessesTab(
                                        viewModel = viewModel,
                                    )

                                    2 -> HistoryTab(
                                        viewModel = viewModel,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRebootConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!isRebooting) {
                    showRebootConfirmation = false
                }
            },
            title = {
                Text(text = stringResource(R.string.server_detail_reboot_confirm_title))
            },
            text = {
                Text(text = stringResource(R.string.server_detail_reboot_confirm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRebootConfirmation = false
                        viewModel.rebootServer()
                    },
                    enabled = !isRebooting,
                ) {
                    Text(text = stringResource(R.string.server_detail_reboot_confirm_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRebootConfirmation = false },
                    enabled = !isRebooting,
                ) {
                    Text(text = stringResource(R.string.server_detail_reboot_cancel_button))
                }
            },
        )
    }
}

@Composable
private fun OverviewTab(
    server: Server,
    isRebooting: Boolean,
    onRebootClick: () -> Unit,
) {
    val ramUnit = stringResource(R.string.server_detail_ram_unit)
    val daysUnit = stringResource(R.string.time_days)
    val hoursUnit = stringResource(R.string.time_hours)
    val minutesUnit = stringResource(R.string.time_minutes)
    val kbUnit = stringResource(R.string.network_bytes_kb)
    val mbUnit = stringResource(R.string.network_bytes_mb)
    val gbUnit = stringResource(R.string.network_bytes_gb)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(Spacing.md))
        ServerHeader(server = server)
        Spacer(modifier = Modifier.height(Spacing.lg))

        KernvoxButton(
            onClick = onRebootClick,
            enabled = !isRebooting,
        ) {
            Text(
                text = stringResource(
                    if (isRebooting) {
                        R.string.server_detail_reboot_in_progress
                    } else {
                        R.string.server_detail_reboot_button
                    }
                )
            )
        }
        Spacer(modifier = Modifier.height(Spacing.lg))

        InfoTile(
            label = stringResource(R.string.server_detail_address),
            value = "${server.host}:${server.port}",
            icon = R.drawable.ic_location,
        )
        Spacer(modifier = Modifier.height(Spacing.sm))

        server.username?.let { username ->
            InfoTile(
                label = stringResource(R.string.server_detail_username),
                value = username,
                icon = R.drawable.ic_server_placeholder,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        server.cpuCores?.let { cores ->
            InfoTile(
                label = stringResource(R.string.server_detail_cpu_cores),
                value = cores.toString(),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        server.cpuPercent?.let { cpu ->
            InfoTile(
                label = stringResource(R.string.server_detail_cpu),
                value = "%.1f%%".format(cpu),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
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
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        server.diskUsedPercent?.let { disk ->
            InfoTile(
                label = stringResource(R.string.server_detail_disk),
                value = "%.1f%%".format(disk),
                icon = R.drawable.ic_server_placeholder,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        if (server.networkRxBytes != null || server.networkTxBytes != null) {
            InfoTile(
                label = stringResource(R.string.server_detail_network_rx),
                value = formatBytes(server.networkRxBytes, kbUnit, mbUnit, gbUnit),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            InfoTile(
                label = stringResource(R.string.server_detail_network_tx),
                value = formatBytes(server.networkTxBytes, kbUnit, mbUnit, gbUnit),
                icon = R.drawable.ic_monitoring,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
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

        Spacer(modifier = Modifier.height(Spacing.sm))

        server.lastMetricTimestamp?.let { ts ->
            InfoTile(
                label = stringResource(R.string.server_detail_last_metric),
                value = formatTimestamp(ts),
                icon = R.drawable.ic_quickview,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun ProcessesTab(
    viewModel: DetailViewModel,
) {
    val processesState by viewModel.processesState.collectAsState()

    AnimatedContent(
        targetState = processesState,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "processes_state",
    ) { state ->
        when (state) {
            is UiState.Loading -> LoadingContent(paddingValues = PaddingValues(Spacing.md))

            is UiState.Error -> ErrorContent(
                title = stringResource(R.string.processes_error_title),
                message = state.message,
                retryLabel = stringResource(R.string.processes_retry),
                onRetry = { viewModel.loadProcesses(force = true) },
                paddingValues = PaddingValues(Spacing.md),
            )

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.processes_empty_title),
                        subtitle = stringResource(R.string.processes_empty_subtitle),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = Spacing.sm),
                    ) {
                        items(
                            items = state.data,
                            key = { it.pid },
                            contentType = { "process_item" },
                        ) { process ->
                            ProcessItem(process = process)
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = androidx.compose.ui.unit.Dp.Hairline,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(
    viewModel: DetailViewModel,
) {
    val historyState by viewModel.historyState.collectAsState()

    AnimatedContent(
        targetState = historyState,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "history_state",
    ) { state ->
        when (state) {
            is UiState.Loading -> LoadingContent(paddingValues = PaddingValues(Spacing.md))

            is UiState.Error -> ErrorContent(
                title = stringResource(R.string.history_error_title),
                message = state.message,
                retryLabel = stringResource(R.string.history_retry),
                onRetry = { viewModel.loadHistory(force = true) },
                paddingValues = PaddingValues(Spacing.md),
            )

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.history_empty_title),
                        subtitle = stringResource(R.string.history_empty_subtitle),
                    )
                } else {
                    val historyEntries = remember(state.data) { state.data.asReversed() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        items(
                            items = historyEntries,
                            key = { it.id },
                            contentType = { "history_row" },
                        ) { entry ->
                            MetricHistoryRow(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerDetailTopAppBar(
    serverName: String,
    onNavigateBack: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = {
            Text(
                text = serverName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onSurface,
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
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = server.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        StatusBadge(isOnline = server.isAvailable ?: false)
    }
}
