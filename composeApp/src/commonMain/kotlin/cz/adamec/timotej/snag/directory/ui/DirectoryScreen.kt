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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRouteFactory
import cz.adamec.timotej.snag.lib.design.fe.api.navigation.SnagNavDisplay
import cz.adamec.timotej.snag.users.fe.driving.api.UsersRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import snag.composeapp.generated.resources.Res
import snag.composeapp.generated.resources.clients_tab_title
import snag.composeapp.generated.resources.users_tab_title
import snag.lib.design.fe.api.generated.resources.ic_engineering
import snag.lib.design.fe.api.generated.resources.ic_engineering_filled
import snag.lib.design.fe.api.generated.resources.ic_work
import snag.lib.design.fe.api.generated.resources.ic_work_filled
import snag.lib.design.fe.api.generated.resources.Res as DesignRes

private const val USERS_TAB_INDEX = 0
private const val CLIENTS_TAB_INDEX = 1

@Suppress("CognitiveComplexMethod")
@Composable
internal fun DirectoryScreen(modifier: Modifier = Modifier) {
    val backStack: DirectoryBackStack = koinInject()
    val clientsRouteFactory = koinInject<ClientsRouteFactory>()
    val clientCreationRouteFactory = koinInject<ClientCreationRouteFactory>()
    val clientEditRouteFactory = koinInject<ClientEditRouteFactory>()

    val backStackContainsClients =
        backStack.value
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

    Column(
        modifier = modifier.systemBarsPadding(),
    ) {
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
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SnagNavDisplay(
            backStack = backStack,
        )
    }
}
