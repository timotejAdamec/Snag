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
import cz.adamec.timotej.snag.clients.fe.app.api.CanDeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.model.SaveClientRequest
import cz.adamec.timotej.snag.core.business.rules.api.EmailFormatRule
import cz.adamec.timotej.snag.core.business.rules.api.PhoneNumberRule
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.error_field_required
import snag.lib.design.fe.generated.resources.error_invalid_email
import snag.lib.design.fe.generated.resources.error_invalid_phone
import kotlin.uuid.Uuid

internal class ClientDetailsEditViewModel(
    @InjectedParam private val clientId: Uuid?,
    private val getClientUseCase: GetClientUseCase,
    private val saveClientUseCase: SaveClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
    private val canDeleteClientUseCase: CanDeleteClientUseCase,
    private val emailFormatRule: EmailFormatRule,
    private val phoneNumberRule: PhoneNumberRule,
) : ViewModel() {
    private val vmState: MutableStateFlow<ClientDetailsEditVmState> =
        MutableStateFlow(ClientDetailsEditVmState())
    val state: StateFlow<ClientDetailsEditUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    init {
        clientId?.let {
            collectClient(it)
            checkCanDelete(it)
        }
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
                            vmState.update {
                                it.copy(
                                    clientName = data.name,
                                    clientAddress = data.address.orEmpty(),
                                    clientPhoneNumber = data.phoneNumber.orEmpty(),
                                    clientEmail = data.email.orEmpty(),
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    private fun checkCanDelete(clientId: Uuid) =
        viewModelScope.launch {
            when (val result = canDeleteClientUseCase(clientId)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    vmState.update { it.copy(canDelete = false) }
                }
                is OfflineFirstDataResult.Success -> {
                    vmState.update { it.copy(canDelete = result.data) }
                }
            }
        }

    fun onClientNameChange(updatedName: String) {
        vmState.update { it.copy(clientName = updatedName, clientNameError = null) }
    }

    fun onClientAddressChange(updatedAddress: String) {
        vmState.update { it.copy(clientAddress = updatedAddress) }
    }

    fun onClientPhoneNumberChange(updatedPhoneNumber: String) {
        vmState.update { it.copy(clientPhoneNumber = updatedPhoneNumber, clientPhoneNumberError = null) }
    }

    fun onClientEmailChange(updatedEmail: String) {
        vmState.update { it.copy(clientEmail = updatedEmail, clientEmailError = null) }
    }

    fun onDelete() {
        val id = clientId ?: return
        viewModelScope.launch {
            vmState.update { it.copy(isBeingDeleted = true) }
            when (deleteClientUseCase(id)) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    vmState.update { it.copy(isBeingDeleted = false) }
                    errorEventsChannel.send(Unknown)
                }
                is OfflineFirstDataResult.Success -> {
                    vmState.update { it.copy(isBeingDeleted = false) }
                    deletedSuccessfullyEventChannel.send(Unit)
                }
            }
        }
    }

    fun onSaveClient() =
        viewModelScope.launch {
            val current = vmState.value
            val nameError = if (current.clientName.isBlank()) Res.string.error_field_required else null
            val phoneError =
                if (current.clientPhoneNumber.isNotBlank() && !phoneNumberRule(current.clientPhoneNumber)) {
                    Res.string.error_invalid_phone
                } else {
                    null
                }
            val emailError =
                if (current.clientEmail.isNotBlank() && !emailFormatRule(current.clientEmail)) {
                    Res.string.error_invalid_email
                } else {
                    null
                }

            if (nameError != null || phoneError != null || emailError != null) {
                vmState.update {
                    it.copy(
                        clientNameError = nameError,
                        clientPhoneNumberError = phoneError,
                        clientEmailError = emailError,
                    )
                }
            } else {
                saveClient()
            }
        }

    private suspend fun saveClient() {
        val current = vmState.value
        val result =
            saveClientUseCase(
                request =
                    SaveClientRequest(
                        id = clientId,
                        name = current.clientName,
                        address = current.clientAddress.ifBlank { null },
                        phoneNumber = current.clientPhoneNumber.ifBlank { null },
                        email = current.clientEmail.ifBlank { null },
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
