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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPane
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneDefaults
import cz.adamec.timotej.snag.lib.design.fe.adaptive.ContentPaneSpacing
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.layout.systemBarsPaddingCoerceAtLeast
import cz.adamec.timotej.snag.lib.design.fe.scenes.LocalDialogPortal
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavigation
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.ui.components.TabItem
import cz.adamec.timotej.snag.users.fe.driving.api.UsersNavigation
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import cz.adamec.timotej.snag.vm.MainViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
internal fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    projectsRoute: ProjectsRoute = koinInject(),
    usersRoute: UsersRoute = koinInject(),
) {
    val syncStatus by mainViewModel.syncStatus.collectAsStateWithLifecycle()
    var dialogContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    SnagTheme {
        CompositionLocalProvider(LocalDialogPortal provides { dialogContent = it }) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppScaffold(
                    containerColor = ContentPaneDefaults.containerColor,
                ) { paddingValues ->
                    MainScreenScaffold(
                        paddingValues = paddingValues,
                        syncBarState = syncStatus.toBarState(),
                        projectsRoute = projectsRoute,
                        usersRoute = usersRoute,
                    )
                }
                dialogContent?.let { content ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA),
                                ),
                    ) {}
                    content()
                }
            }
        }
    }
}

@Composable
private fun MainScreenScaffold(
    paddingValues: PaddingValues,
    syncBarState: SyncStatusBarState,
    projectsRoute: ProjectsRoute,
    usersRoute: UsersRoute,
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

        val tintedBackground = ContentPaneDefaults.containerColor
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
                    route = usersRoute,
                    selected = currentDestination == TopLevelDestination.USERS,
                    onClick = {
                        currentDestination = TopLevelDestination.USERS
                    },
                )
            },
        ) {
            val content: @Composable () -> Unit = {
                when (currentDestination) {
                    TopLevelDestination.PROJECTS -> ProjectsNavigation()
                    TopLevelDestination.USERS -> UsersNavigation()
                }
            }
            if (isScreenWide()) {
                ContentPane(
                    modifier =
                        Modifier.systemBarsPaddingCoerceAtLeast(
                            top = ContentPaneSpacing,
                            end = ContentPaneSpacing,
                            bottom = ContentPaneSpacing,
                        ),
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.32f
