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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.model.SaveProjectRequest
import kotlinx.collections.immutable.toImmutableList
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
import kotlin.uuid.Uuid

internal class ProjectDetailsEditViewModel(
    @InjectedParam private val projectId: Uuid?,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<ProjectDetailsEditVmState> =
        MutableStateFlow(ProjectDetailsEditVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                listOf(collectClients())
            }
    val state: StateFlow<ProjectDetailsEditUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        projectId?.let { collectProject(it) }
    }

    private fun collectProject(projectId: Uuid) =
        viewModelScope.launch {
            getProjectUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        result.data?.let { data ->
                            vmState.update {
                                it.copy(
                                    projectName = data.name,
                                    projectAddress = data.address,
                                    selectedClientId = data.clientId,
                                    selectedClientName =
                                        data.clientId?.let { clientId ->
                                            resolveClientName(clientId)
                                        } ?: "",
                                )
                            }
                            cancel()
                        }
                    }
                }
            }
        }

    private fun collectClients() =
        viewModelScope.launch {
            getClientsUseCase().collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        errorEventsChannel.send(Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        val clients = result.data.toImmutableList()
                        vmState.update { current ->
                            val resolvedName =
                                if (current.selectedClientId != null && current.selectedClientName.isEmpty()) {
                                    val matchingClient =
                                        clients.firstOrNull { it.id == current.selectedClientId }
                                    matchingClient?.name ?: ""
                                } else {
                                    current.selectedClientName
                                }
                            current.copy(
                                availableClients = clients,
                                selectedClientName = resolvedName,
                            )
                        }
                    }
                }
            }
        }

    private fun resolveClientName(clientId: Uuid): String {
        val matchingClient =
            vmState.value.availableClients.firstOrNull { it.id == clientId }
        return matchingClient?.name ?: ""
    }

    fun onClientSelected(
        clientId: Uuid,
        clientName: String,
    ) {
        vmState.update {
            it.copy(
                selectedClientId = clientId,
                selectedClientName = clientName,
            )
        }
    }

    fun onClientCleared() {
        vmState.update {
            it.copy(
                selectedClientId = null,
                selectedClientName = "",
            )
        }
    }

    fun onClientCreated(clientId: Uuid) {
        vmState.update {
            it.copy(
                selectedClientId = clientId,
                selectedClientName = resolveClientName(clientId),
            )
        }
    }

    fun onProjectNameChange(updatedName: String) {
        vmState.update { it.copy(projectName = updatedName, projectNameError = null) }
    }

    fun onProjectAddressChange(updatedAddress: String) {
        vmState.update { it.copy(projectAddress = updatedAddress, projectAddressError = null) }
    }

    fun onSaveProject() =
        viewModelScope.launch {
            val current = vmState.value
            val nameError = if (current.projectName.isBlank()) Res.string.error_field_required else null
            val addressError = if (current.projectAddress.isBlank()) Res.string.error_field_required else null

            if (nameError != null || addressError != null) {
                vmState.update {
                    it.copy(
                        projectNameError = nameError,
                        projectAddressError = addressError,
                    )
                }
            } else {
                saveProject()
            }
        }

    private suspend fun saveProject() {
        val current = vmState.value
        val result =
            saveProjectUseCase(
                request =
                    SaveProjectRequest(
                        id = projectId,
                        name = current.projectName,
                        address = current.projectAddress,
                        clientId = current.selectedClientId,
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
