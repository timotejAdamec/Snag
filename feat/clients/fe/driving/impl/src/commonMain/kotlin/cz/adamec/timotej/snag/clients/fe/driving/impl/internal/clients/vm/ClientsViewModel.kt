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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class ClientsViewModel(
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<ClientsUiState> = MutableStateFlow(ClientsUiState())
    val state: StateFlow<ClientsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    init {
        collectClients()
    }

    private fun collectClients() =
        getClientsUseCase()
            .map { clientsDataResult ->
                when (clientsDataResult) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                            )
                        }
                        errorEventsChannel.send(UiError.Unknown)
                    }

                    is OfflineFirstDataResult.Success -> {
                        _state.update {
                            it.copy(
                                clients = clientsDataResult.data.toPersistentList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
}
