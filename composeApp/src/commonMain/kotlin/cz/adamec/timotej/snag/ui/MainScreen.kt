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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.lib.design.fe.scaffold.AppScaffold
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBar
import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import cz.adamec.timotej.snag.lib.navigation.fe.TabNavRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.ui.navigation.ProjectsNavigation
import cz.adamec.timotej.snag.ui.navigation.UsersNavigation
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import cz.adamec.timotej.snag.vm.MainViewModel
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private enum class TopLevelDestination {
    PROJECTS,
    USERS,
}

@OptIn(FlowPreview::class)
@Composable
internal fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    projectsRoute: ProjectsRoute = koinInject(),
    usersRoute: UsersRoute = koinInject(),
) {
    val syncStatus by mainViewModel.syncStatus.collectAsStateWithLifecycle()
    SnagTheme {
        AppScaffold { paddingValues ->
            MainScreenContent(
                paddingValues = paddingValues,
                syncBarState = syncStatus.toBarState(),
                projectsRoute = projectsRoute,
                usersRoute = usersRoute,
            )
        }
    }
}

@Composable
private fun MainScreenContent(
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

        NavigationSuiteScaffold(
            modifier = navigationModifier,
            navigationSuiteItems = {
                tabItem(projectsRoute, currentDestination == TopLevelDestination.PROJECTS) {
                    currentDestination = TopLevelDestination.PROJECTS
                }
                tabItem(usersRoute, currentDestination == TopLevelDestination.USERS) {
                    currentDestination = TopLevelDestination.USERS
                }
            },
        ) {
            Box(modifier = tabModifier(currentDestination == TopLevelDestination.PROJECTS)) {
                ProjectsNavigation()
            }
            Box(modifier = tabModifier(currentDestination == TopLevelDestination.USERS)) {
                UsersNavigation()
            }
        }
    }
}

private fun tabModifier(isSelected: Boolean): Modifier = if (isSelected) Modifier.fillMaxSize() else Modifier.size(0.dp)

private fun NavigationSuiteScope.tabItem(
    route: TabNavRoute,
    selected: Boolean,
    onClick: () -> Unit,
) {
    item(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                painter = painterResource(route.tabIcon),
                contentDescription = null,
            )
        },
        label = {
            Text(stringResource(route.tabLabel))
        },
    )
}
