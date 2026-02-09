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

package cz.adamec.timotej.snag.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus
import cz.adamec.timotej.snag.ui.navigation.SnagNavigation
import cz.adamec.timotej.snag.vm.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
    val syncStatus by mainViewModel.syncStatus.collectAsStateWithLifecycle()
    SnagTheme {
        AppScaffold {
            Column {
                SyncStatusBar(state = syncStatus.toBarState())
                SnagNavigation()
            }
        }
    }
}

private fun SyncStatus.toBarState(): SyncStatusBarState =
    when (this) {
        SyncStatus.Synced -> SyncStatusBarState.SYNCED
        SyncStatus.Syncing -> SyncStatusBarState.SYNCING
        SyncStatus.Offline -> SyncStatusBarState.OFFLINE
        SyncStatus.Error -> SyncStatusBarState.ERROR
    }
