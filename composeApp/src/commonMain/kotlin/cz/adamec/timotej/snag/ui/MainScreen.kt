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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneDefaults
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavigation
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.ui.components.TabItem
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryNavigation
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryRoute
import cz.adamec.timotej.snag.vm.MainViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
internal fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    projectsRoute: ProjectsRoute = koinInject(),
    directoryRoute: DirectoryRoute = koinInject(),
) {
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
                projectsRoute = projectsRoute,
                directoryRoute = directoryRoute,
            )
        }
    }
}

@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    syncBarState: SyncStatusBarState,
    projectsRoute: ProjectsRoute,
    directoryRoute: DirectoryRoute,
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

        var currentDestination by rememberSaveable {
            mutableStateOf(TopLevelDestination.PROJECTS)
        }

        val tintedBackground =
            if (isScreenWide()) {
                ContentPaneDefaults.containerColor
            } else {
                ContentPaneDefaults.paneColor
            }
        NavigationSuiteScaffold(
            modifier = navigationModifier,
            containerColor = tintedBackground,
            navigationSuiteColors =
                NavigationSuiteDefaults.colors(
                    wideNavigationRailColors =
                        WideNavigationRailDefaults.colors(
                            containerColor = tintedBackground,
                        ),
                ),
            navigationItems = {
                TabItem(
                    route = projectsRoute,
                    selected = currentDestination == TopLevelDestination.PROJECTS,
                    onClick = {
                        currentDestination = TopLevelDestination.PROJECTS
                    },
                )
                TabItem(
                    route = directoryRoute,
                    selected = currentDestination == TopLevelDestination.DIRECTORY,
                    onClick = {
                        currentDestination = TopLevelDestination.DIRECTORY
                    },
                )
            },
        ) {
            when (currentDestination) {
                TopLevelDestination.PROJECTS -> ProjectsNavigation()
                TopLevelDestination.DIRECTORY -> DirectoryNavigation()
            }
        }
    }
}
