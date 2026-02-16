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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.ui.navigation.SnagNavigation
import cz.adamec.timotej.snag.vm.MainViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
internal fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
    val syncStatus by mainViewModel.syncStatus.collectAsStateWithLifecycle()
    SnagTheme {
        AppScaffold { paddingValues ->
            Column {
                var isSyncStatusBarVisible by remember { mutableStateOf(false) }

                val syncBarTopContentPaddingValues =
                    PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                    )
                SyncStatusBar(
                    modifier = Modifier,
                    state = syncStatus.toBarState(),
                    contentPadding = syncBarTopContentPaddingValues,
                    onVisibilityChange = { isVisible ->
                        isSyncStatusBarVisible = isVisible
                    }
                )

                val navigationModifier =
                    if (isSyncStatusBarVisible) {
                        Modifier.consumeWindowInsets(syncBarTopContentPaddingValues)
                    } else {
                        Modifier
                    }
                SnagNavigation(
                    modifier = navigationModifier,
                )
            }
        }
    }
}
