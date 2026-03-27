package com.vennilay.kernvox.ui.screens.serverDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.data.model.Server
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.StatusBadge
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import com.vennilay.kernvox.ui.utils.formatLastChecked
import com.vennilay.kernvox.ui.utils.formatUptime

/**
 * Экран детальной информации о сервере.
 *
 * @param server Данные сервера для отображения
 * @param onNavigateBack Обработчик навигации назад
 * @param modifier Модификатор для компонента
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    server: Server,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ServerDetailTopAppBar(
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок с иконкой и статусом
            ServerHeader(server = server)

            Spacer(modifier = Modifier.height(24.dp))

            // Плитка: Адрес
            DetailTile(
                label = "Адрес",
                value = "${server.host}:${server.port}",
                icon = R.drawable.ic_location
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Плитка: ID сервера
            DetailTile(
                label = "ID сервера",
                value = server.id,
                icon = R.drawable.ic_server_placeholder
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Плитка: Аптайм
            DetailTile(
                label = "Аптайм",
                value = formatUptime(server.uptimeSeconds),
                icon = R.drawable.ic_uptime
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Плитка: Последняя проверка
            DetailTile(
                label = "Последняя проверка",
                value = formatLastChecked(server.lastCheckedAtEpochMillis),
                icon = R.drawable.ic_quickview
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerDetailTopAppBar(
    onNavigateBack: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.back_button),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ServerHeader(
    server: Server
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Иконка сервера
        IconCircle(
            icon = R.drawable.ic_server_placeholder,
            containerSize = 80,
            iconSize = 40
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Название сервера
        Text(
            text = server.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Бейдж статуса
        StatusBadge(isOnline = server.isAvailable)
    }
}

/**
 * Универсальная плитка для отображения информации.
 */
@Composable
private fun DetailTile(
    label: String,
    value: String,
    icon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(icon = icon)

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerDetailScreenPreview() {
    KernvoxTheme {
        ServerDetailScreen(
            server = Server(
                id = "srv-1",
                name = "Main Gateway",
                host = "192.168.1.1",
                port = 8080,
                uptimeSeconds = 125340,
                isAvailable = true,
                lastCheckedAtEpochMillis = System.currentTimeMillis()
            ),
            onNavigateBack = {}
        )
    }
}
