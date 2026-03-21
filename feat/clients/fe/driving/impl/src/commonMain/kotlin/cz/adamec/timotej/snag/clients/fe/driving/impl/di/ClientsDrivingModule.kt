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

package cz.adamec.timotej.snag.clients.fe.driving.impl.di

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.ui.ClientDetailsEditScreen
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm.ClientDetailsEditViewModel
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui.ClientsScreen
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm.ClientsViewModel
import cz.adamec.timotej.snag.lib.design.fe.dialog.fullscreenDialogProperties
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryBackStack
import cz.adamec.timotej.snag.users.fe.driving.api.DirectoryRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import snag.feat.clients.fe.driving.impl.generated.resources.Res
import snag.feat.clients.fe.driving.impl.generated.resources.clients_tab_title
import snag.feat.clients.fe.driving.impl.generated.resources.users_tab_title
import kotlin.uuid.Uuid

private const val USERS_TAB_INDEX = 0
private const val CLIENTS_TAB_INDEX = 1

internal inline fun <reified T : ClientsRoute> Module.clientsScreenNavigation() =
    navigation<T> {
        val backStack = get<DirectoryBackStack>()
        val directoryRoute = get<DirectoryRoute>()
        val clientCreationRouteFactory = get<ClientCreationRouteFactory>()
        val clientEditRouteFactory = get<ClientEditRouteFactory>()
        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryTabRow(selectedTabIndex = CLIENTS_TAB_INDEX) {
                Tab(
                    selected = false,
                    onClick = {
                        backStack.value[backStack.value.lastIndex] = directoryRoute
                    },
                    text = { Text(text = stringResource(Res.string.users_tab_title)) },
                )
                Tab(
                    selected = true,
                    onClick = { },
                    text = { Text(text = stringResource(Res.string.clients_tab_title)) },
                )
            }
            ClientsScreen(
                modifier = Modifier.fillMaxSize(),
                onNewClientClick = {
                    backStack.value.add(
                        clientCreationRouteFactory.create(
                            onCreated = { },
                            onDismiss = { backStack.removeLastSafely() },
                        ),
                    )
                },
                onClientClick = { clientId ->
                    backStack.value.add(clientEditRouteFactory.create(clientId))
                },
            )
        }
    }

@Suppress("FunctionNameMaxLength")
internal inline fun <reified T : ClientCreationRoute> Module.clientCreationScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        ClientDetailsEditScreenInjection(
            onSaveClient = { savedClientId ->
                route.onCreated(savedClientId)
                route.onDismiss()
            },
            onCancelClick = { route.onDismiss() },
        )
    }

internal inline fun <reified T : ClientEditRoute> Module.clientEditScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        val backStack = get<DirectoryBackStack>()
        ClientDetailsEditScreenInjection(
            clientId = route.clientId,
            onSaveClient = { _ ->
                backStack.removeLastSafely()
            },
            onCancelClick = { backStack.removeLastSafely() },
            onDeleteClient = {
                backStack.removeLastSafely()
            },
        )
    }

@Composable
@Suppress("FunctionNameMaxLength")
private fun ClientDetailsEditScreenInjection(
    onSaveClient: (savedClientId: Uuid) -> Unit,
    onCancelClick: () -> Unit,
    clientId: Uuid? = null,
    onDeleteClient: (() -> Unit)? = null,
) {
    ClientDetailsEditScreen(
        clientId = clientId,
        onSaveClient = { savedClientId ->
            onSaveClient(savedClientId)
        },
        onCancelClick = onCancelClick,
        onDeleteClient = onDeleteClient,
    )
}

val clientsDrivingImplModule =
    module {
        includes(platformModule)
        viewModelOf(::ClientsViewModel)
        viewModel { (clientId: Uuid?) ->
            ClientDetailsEditViewModel(
                clientId = clientId,
                getClientUseCase = get(),
                saveClientUseCase = get(),
                deleteClientUseCase = get(),
                canDeleteClientUseCase = get(),
                emailFormatRule = get(),
                phoneNumberRule = get(),
            )
        }
    }

internal expect val platformModule: Module
