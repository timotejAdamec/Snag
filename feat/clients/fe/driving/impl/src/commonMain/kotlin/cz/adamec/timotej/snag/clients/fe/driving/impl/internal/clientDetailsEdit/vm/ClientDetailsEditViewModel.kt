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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.model.SaveClientRequest
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.CustomUserMessage
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class ClientDetailsEditViewModel(
    @InjectedParam private val clientId: Uuid?,
    private val getClientUseCase: GetClientUseCase,
    private val saveClientUseCase: SaveClientUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<ClientDetailsEditUiState> =
        MutableStateFlow(ClientDetailsEditUiState())
    val state: StateFlow<ClientDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        clientId?.let { collectClient(it) }
    }

    private fun collectClient(clientId: Uuid) =
        viewModelScope.launch {
            getClientUseCase(clientId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            _state.update {
                                it.copy(
                                    clientName = data.client.name,
                                    clientAddress = data.client.address.orEmpty(),
                                    clientPhoneNumber = data.client.phoneNumber.orEmpty(),
                                    clientEmail = data.client.email.orEmpty(),
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    fun onClientNameChange(updatedName: String) {
        _state.update { it.copy(clientName = updatedName) }
    }

    fun onClientAddressChange(updatedAddress: String) {
        _state.update { it.copy(clientAddress = updatedAddress) }
    }

    fun onClientPhoneNumberChange(updatedPhoneNumber: String) {
        _state.update { it.copy(clientPhoneNumber = updatedPhoneNumber) }
    }

    fun onClientEmailChange(updatedEmail: String) {
        _state.update { it.copy(clientEmail = updatedEmail) }
    }

    fun onSaveClient() =
        viewModelScope.launch {
            if (state.value.clientName.isBlank()) {
                errorEventsChannel.send(CustomUserMessage("Client name cannot be empty"))
            } else {
                saveClient()
            }
        }

    private suspend fun saveClient() {
        val result =
            saveClientUseCase(
                request =
                    SaveClientRequest(
                        id = clientId,
                        name = state.value.clientName,
                        address = state.value.clientAddress.ifBlank { null },
                        phoneNumber = state.value.clientPhoneNumber.ifBlank { null },
                        email = state.value.clientEmail.ifBlank { null },
                    ),
            )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                saveEventChannel.send(result.data)
            }
        }
    }
}
