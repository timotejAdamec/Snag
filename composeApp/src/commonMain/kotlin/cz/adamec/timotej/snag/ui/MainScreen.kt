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
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.directory.ui.DirectoryNavRoute
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneDefaults
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavRoute
import cz.adamec.timotej.snag.ui.components.TabItem
import cz.adamec.timotej.snag.vm.MainViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
internal fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
    val syncStatus by mainViewModel.syncStatus.collectAsStateWithLifecycle()
    SnagTheme {
        val outerContainerColor =
            if (isScreenWide()) {
                ContentPaneDefaults.containerColor
            } else {
                ContentPaneDefaults.paneColor
            }
        AppScaffold(
            containerColor = outerContainerColor,
        ) { paddingValues ->
            MainScreenContent(
                paddingValues = paddingValues,
                syncBarState = syncStatus.toBarState(),
                outerContainerColor = outerContainerColor,
            )
        }
    }
}

@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    syncBarState: SyncStatusBarState,
    outerContainerColor: Color,
) {
    Column {
        var isSyncStatusBarVisible by remember { mutableStateOf(false) }

        val syncBarTopContentPaddingValues =
            PaddingValues(
                top = paddingValues.calculateTopPadding(),
            )
        SyncStatusBar(
            modifier = Modifier,
            state = syncBarState,
            contentPadding = syncBarTopContentPaddingValues,
            onVisibilityChange = { isVisible ->
                isSyncStatusBarVisible = isVisible
            },
        )

        val navigationModifier =
            if (isSyncStatusBarVisible) {
                Modifier.consumeWindowInsets(syncBarTopContentPaddingValues)
            } else {
                Modifier
            }

        val mainBackStack = koinInject<MainBackStack>()
        NavigationSuiteScaffold(
            modifier = navigationModifier,
            containerColor = outerContainerColor,
            navigationSuiteColors =
                NavigationSuiteDefaults.colors(
                    wideNavigationRailColors =
                        WideNavigationRailDefaults.colors(
                            containerColor = outerContainerColor,
                        ),
                ),
            navigationItems = {
                TabItem(
                    topLevelDestination = TopLevelDestination.Project,
                    selected = mainBackStack.value.last() is ProjectsNavRoute,
                    onClick = {
                        if (mainBackStack.value.last() is DirectoryNavRoute) {
                            mainBackStack.value.removeLastOrNull()
                        }
                    },
                )
                TabItem(
                    topLevelDestination = TopLevelDestination.Directory,
                    selected = mainBackStack.value.last() is DirectoryNavRoute,
                    onClick = {
                        if (mainBackStack.value.last() !is DirectoryNavRoute) {
                            mainBackStack.value.add(
                                DirectoryNavRoute(
                                    onExit = {
                                        mainBackStack.removeLastSafely()
                                    },
                                ),
                            )
                        }
                    },
                )
            },
        ) {
            MainNavigation()
        }
    }
}
