/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.lib.design.fe.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagPreview
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.ic_cloud_alert
import snag.lib.design.fe.generated.resources.ic_cloud_done
import snag.lib.design.fe.generated.resources.ic_cloud_off
import snag.lib.design.fe.generated.resources.sync_status_error
import snag.lib.design.fe.generated.resources.sync_status_offline
import snag.lib.design.fe.generated.resources.sync_status_synced
import snag.lib.design.fe.generated.resources.sync_status_syncing

private const val SYNCED_DISPLAY_DURATION_MS = 2_000L
private val ICON_SIZE = 16.dp

@Composable
fun SyncStatusBar(
    state: SyncStatusBarState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onVisibilityChange: (Boolean) -> Unit = {},
) {
    var hasShownNonSynced by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state == SyncStatusBarState.SYNCED) {
            if (hasShownNonSynced) {
                isVisible = true
                delay(SYNCED_DISPLAY_DURATION_MS)
                isVisible = false
            }
        } else {
            hasShownNonSynced = true
            isVisible = true
        }
    }

    LaunchedEffect(isVisible) {
        onVisibilityChange(isVisible)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        SyncStatusBarContent(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun SyncStatusBarContent(
    state: SyncStatusBarState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val backgroundColor: Color
    val contentColor: Color
    val label: String
    val icon: @Composable () -> Unit

    when (state) {
        SyncStatusBarState.SYNCED -> {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            label = stringResource(Res.string.sync_status_synced)
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_cloud_done),
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = contentColor,
                )
            }
        }

        SyncStatusBarState.SYNCING -> {
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            label = stringResource(Res.string.sync_status_syncing)
            icon = {
                LoadingIndicator(
                    modifier = Modifier.size(ICON_SIZE),
                    color = contentColor,
                )
            }
        }

        SyncStatusBarState.OFFLINE -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            label = stringResource(Res.string.sync_status_offline)
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_cloud_off),
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = contentColor,
                )
            }
        }

        SyncStatusBarState.ERROR -> {
            backgroundColor = MaterialTheme.colorScheme.errorContainer
            contentColor = MaterialTheme.colorScheme.onErrorContainer
            label = stringResource(Res.string.sync_status_error)
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_cloud_alert),
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = contentColor,
                )
            }
        }
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        icon()
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview
@Composable
private fun SyncStatusBarSyncedPreview() {
    SnagPreview {
        SyncStatusBarContent(state = SyncStatusBarState.SYNCED)
    }
}

@Preview
@Composable
private fun SyncStatusBarSyncingPreview() {
    SnagPreview {
        SyncStatusBarContent(state = SyncStatusBarState.SYNCING)
    }
}

@Preview
@Composable
private fun SyncStatusBarOfflinePreview() {
    SnagPreview {
        SyncStatusBarContent(state = SyncStatusBarState.OFFLINE)
    }
}

@Preview
@Composable
private fun SyncStatusBarErrorPreview() {
    SnagPreview {
        SyncStatusBarContent(state = SyncStatusBarState.ERROR)
    }
}
