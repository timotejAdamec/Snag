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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.ui.ClientDetailsEditScreen
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm.ClientDetailsEditViewModel
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui.ClientsScreen
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm.ClientsViewModel
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : ClientsRoute> Module.clientsScreenNavigation() =
    navigation<T> {
        ClientsScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
            onNewClientClick = {
                val backStack = get<SnagBackStack>()
                val clientCreationRoute = get<ClientCreationRoute>()
                backStack.value.add(clientCreationRoute)
            },
            onClientClick = { clientId ->
                val backStack = get<SnagBackStack>()
                val clientEditRoute = get<ClientEditRouteFactory>().create(clientId)
                backStack.value.add(clientEditRoute)
            },
        )
    }

@Suppress("FunctionNameMaxLength")
internal inline fun <reified T : ClientCreationRoute> Module.clientCreationScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { _ ->
        ClientDetailsEditScreenInjection(
            onSaveClient = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

internal inline fun <reified T : ClientEditRoute> Module.clientEditScreenNavigation() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        ClientDetailsEditScreenInjection(
            clientId = route.clientId,
            onSaveClient = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Composable
@Suppress("FunctionNameMaxLength")
private fun Scope.ClientDetailsEditScreenInjection(
    clientId: Uuid? = null,
    onSaveClient: (savedClientId: Uuid) -> Unit,
) {
    ClientDetailsEditScreen(
        clientId = clientId,
        onSaveClient = { savedClientId ->
            onSaveClient(savedClientId)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
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
            )
        }
    }

internal expect val platformModule: Module
