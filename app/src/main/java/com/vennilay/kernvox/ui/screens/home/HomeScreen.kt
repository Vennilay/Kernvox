package com.vennilay.kernvox.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vennilay.kernvox.R
import com.vennilay.kernvox.ui.components.IconCircle
import com.vennilay.kernvox.ui.components.KernvoxButton
import com.vennilay.kernvox.ui.theme.KernvoxTheme
import com.vennilay.kernvox.ui.theme.Spacing

@Composable
fun HomeScreen(
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(400),
                ) + fadeIn(tween(400)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg)
                        .padding(top = Spacing.lg, bottom = Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    IconCircle(
                        icon = R.drawable.ic_server_placeholder,
                        containerSize = 120,
                        iconSize = 60,
                        rounded = true,
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Text(
                        text = stringResource(R.string.home_welcome_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    InfoCard()

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    FeaturesList()

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    KernvoxButton(
                        onClick = onOpenApp,
                        modifier = Modifier.widthIn(max = 400.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_server_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(Spacing.md),
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = stringResource(R.string.home_open_app_button),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.md, vertical = 20.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_description_line_1),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.home_description_line_2),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun FeaturesList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_features_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FeatureItem(
            icon = R.drawable.ic_monitoring,
            text = stringResource(R.string.home_feature_monitoring),
        )

        FeatureItem(
            icon = R.drawable.ic_uptime,
            text = stringResource(R.string.home_feature_uptime),
        )

        FeatureItem(
            icon = R.drawable.ic_quickview,
            text = stringResource(R.string.home_feature_quickview),
        )
    }
}

@Composable
private fun FeatureItem(
    icon: Int,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm + Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(
            icon = icon,
            containerSize = 36,
            iconSize = 18,
            rounded = false,
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KernvoxTheme {
        HomeScreen(onOpenApp = {})
    }
}
