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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.directory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui.ClientsScreen
import cz.adamec.timotej.snag.lib.design.fe.error.ShowSnackbarOnError
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryBackStack
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui.UserManagementContent
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserManagementViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import snag.feat.users.fe.driving.impl.generated.resources.Res
import snag.feat.users.fe.driving.impl.generated.resources.clients_tab_title
import snag.feat.users.fe.driving.impl.generated.resources.users_tab_title

private const val USERS_TAB_INDEX = 0
private const val CLIENTS_TAB_INDEX = 1

@Composable
internal fun DirectoryScreen(
    modifier: Modifier = Modifier,
    userManagementViewModel: UserManagementViewModel = koinViewModel(),
    backStack: DirectoryBackStack = koinInject(),
    clientCreationRouteFactory: ClientCreationRouteFactory = koinInject(),
    clientEditRouteFactory: ClientEditRouteFactory = koinInject(),
) {
    val userManagementState by userManagementViewModel.state.collectAsStateWithLifecycle()

    ShowSnackbarOnError(userManagementViewModel.errorsFlow)

    var selectedTab by rememberSaveable { mutableIntStateOf(USERS_TAB_INDEX) }

    Column(modifier = modifier) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == USERS_TAB_INDEX,
                onClick = { selectedTab = USERS_TAB_INDEX },
                text = { Text(text = stringResource(Res.string.users_tab_title)) },
            )
            Tab(
                selected = selectedTab == CLIENTS_TAB_INDEX,
                onClick = { selectedTab = CLIENTS_TAB_INDEX },
                text = { Text(text = stringResource(Res.string.clients_tab_title)) },
            )
        }

        when (selectedTab) {
            USERS_TAB_INDEX ->
                UserManagementContent(
                    modifier = Modifier.fillMaxSize(),
                    state = userManagementState,
                    onRoleChange = userManagementViewModel::onRoleChanged,
                )

            CLIENTS_TAB_INDEX ->
                ClientsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNewClientClick = {
                        backStack.value.add(clientCreationRouteFactory.create { })
                    },
                    onClientClick = { clientId ->
                        backStack.value.add(clientEditRouteFactory.create(clientId))
                    },
                )
        }
    }
}
