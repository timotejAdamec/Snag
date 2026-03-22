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

package cz.adamec.timotej.snag.directory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRouteFactory
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import snag.composeapp.generated.resources.Res
import snag.composeapp.generated.resources.clients_tab_title
import snag.composeapp.generated.resources.users_tab_title
import snag.lib.design.fe.generated.resources.ic_engineering
import snag.lib.design.fe.generated.resources.ic_engineering_filled
import snag.lib.design.fe.generated.resources.ic_work
import snag.lib.design.fe.generated.resources.ic_work_filled
import snag.lib.design.fe.generated.resources.Res as DesignRes

private const val USERS_TAB_INDEX = 0
private const val CLIENTS_TAB_INDEX = 1

@Composable
internal fun DirectoryScreen(
    modifier: Modifier = Modifier,
) {
    val backStack: DirectoryBackStack = koinInject()
    val backStackEntriesState = remember { mutableStateOf(backStack.value) }
    val clientsRouteFactory = koinInject<ClientsRouteFactory>()
    val clientCreationRouteFactory = koinInject<ClientCreationRouteFactory>()
    val clientEditRouteFactory = koinInject<ClientEditRouteFactory>()

    val backStackContainsClients =
        backStackEntriesState.value
            .filterIsInstance<ClientsRoute>()
            .isNotEmpty()
    val selectedTab =
        if (backStackContainsClients) CLIENTS_TAB_INDEX else USERS_TAB_INDEX

    val clientsRoute =
        clientsRouteFactory.create(
            onNewClientClick = {
                backStack.value.add(
                    clientCreationRouteFactory.create(
                        onCreated = { },
                        onDismiss = { backStack.removeLastSafely() },
                    ),
                )
            },
            onClientClick = { clientId ->
                backStack.value.add(
                    clientEditRouteFactory.create(
                        clientId = clientId,
                        onDismiss = { backStack.removeLastSafely() },
                    ),
                )
            },
        )
    val usersRoute: UsersRoute = koinInject()

    Column(modifier = modifier) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == USERS_TAB_INDEX,
                onClick = {
                    if (selectedTab != USERS_TAB_INDEX) {
                        backStack.value[backStack.value.lastIndex] = usersRoute
                    }
                },
                text = { Text(text = stringResource(Res.string.users_tab_title)) },
                icon = {
                    val iconRes =
                        if (selectedTab == USERS_TAB_INDEX) {
                            DesignRes.drawable.ic_engineering_filled
                        } else {
                            DesignRes.drawable.ic_engineering
                        }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                    )
                },
            )
            Tab(
                selected = selectedTab == CLIENTS_TAB_INDEX,
                onClick = {
                    if (selectedTab != CLIENTS_TAB_INDEX) {
                        backStack.value[backStack.value.lastIndex] = clientsRoute
                    }
                },
                text = { Text(text = stringResource(Res.string.clients_tab_title)) },
                icon = {
                    val iconRes =
                        if (selectedTab == CLIENTS_TAB_INDEX) {
                            DesignRes.drawable.ic_work_filled
                        } else {
                            DesignRes.drawable.ic_work
                        }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                    )
                },
            )
        }

        val entryProvider = koinEntryProvider<SnagNavRoute>()
        NavDisplay(
            modifier = modifier,
            backStack = backStackEntriesState.value,
            entryProvider = entryProvider,
            sceneStrategies =
                listOf(
                    DialogSceneStrategy(),
                ),
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
        )
    }
}
