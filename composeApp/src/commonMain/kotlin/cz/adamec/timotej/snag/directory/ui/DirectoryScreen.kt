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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRouteFactory
import cz.adamec.timotej.snag.lib.design.fe.scenes.ContentPaneSceneStrategy
import cz.adamec.timotej.snag.lib.design.fe.scenes.InlineDialogSceneStrategy
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import snag.composeapp.generated.resources.Res
import snag.composeapp.generated.resources.clients_tab_title
import snag.composeapp.generated.resources.users_tab_title

private const val USERS_TAB_INDEX = 0
private const val CLIENTS_TAB_INDEX = 1

@Composable
internal fun DirectoryScreen(
    backStack: DirectoryBackStack,
    modifier: Modifier = Modifier,
    usersRoute: UsersRoute = koinInject(),
) {
    val clientsRouteFactory = koinInject<ClientsRouteFactory>()
    val clientCreationRouteFactory = koinInject<ClientCreationRouteFactory>()
    val clientEditRouteFactory = koinInject<ClientEditRouteFactory>()

    val currentTop = backStack.value.lastOrNull()
    val selectedTab =
        if (currentTop is ClientsRoute) CLIENTS_TAB_INDEX else USERS_TAB_INDEX

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

    Column(modifier = modifier) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == USERS_TAB_INDEX,
                onClick = {
                    if (selectedTab != USERS_TAB_INDEX) {
                        backStack.value[backStack.value.lastIndex] = usersRoute
                    }
                },
                text = { Text(text = stringResource(Res.string.users_tab_title)) },
            )
            Tab(
                selected = selectedTab == CLIENTS_TAB_INDEX,
                onClick = {
                    if (selectedTab != CLIENTS_TAB_INDEX) {
                        backStack.value[backStack.value.lastIndex] = clientsRoute
                    }
                },
                text = { Text(text = stringResource(Res.string.clients_tab_title)) },
            )
        }

        val entryProvider = koinEntryProvider<SnagNavRoute>()
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack.value,
            entryProvider = entryProvider,
            sceneStrategies =
                listOf(
                    InlineDialogSceneStrategy(),
                    ContentPaneSceneStrategy(),
                ),
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
        )
    }
}
