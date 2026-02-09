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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.ic_cloud_done
import snag.lib.design.fe.generated.resources.ic_cloud_off
import snag.lib.design.fe.generated.resources.ic_sync_problem
import snag.lib.design.fe.generated.resources.sync_status_error
import snag.lib.design.fe.generated.resources.sync_status_offline
import snag.lib.design.fe.generated.resources.sync_status_synced
import snag.lib.design.fe.generated.resources.sync_status_syncing

private const val SYNCED_DISPLAY_DURATION_MS = 2_000L

@Composable
fun SyncStatusBar(
    state: SyncStatusBarState,
    modifier: Modifier = Modifier,
) {
    var hasShownNonSynced by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (state) {
            SyncStatusBarState.SYNCED -> {
                if (hasShownNonSynced) {
                    visible = true
                    delay(SYNCED_DISPLAY_DURATION_MS)
                    visible = false
                }
            }
            else -> {
                hasShownNonSynced = true
                visible = true
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
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
                        modifier = Modifier.size(16.dp),
                        tint = contentColor,
                    )
                }
            }
            SyncStatusBarState.SYNCING -> {
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                label = stringResource(Res.string.sync_status_syncing)
                icon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = contentColor,
                        strokeWidth = 2.dp,
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
                        modifier = Modifier.size(16.dp),
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
                        painter = painterResource(Res.drawable.ic_sync_problem),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
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
}
