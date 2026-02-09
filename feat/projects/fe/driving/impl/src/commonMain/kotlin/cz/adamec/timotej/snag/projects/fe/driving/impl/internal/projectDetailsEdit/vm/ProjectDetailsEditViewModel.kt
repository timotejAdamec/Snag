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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.CustomUserMessage
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
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
import kotlin.uuid.Uuid

internal class ProjectDetailsEditViewModel(
    @InjectedParam private val projectId: Uuid?,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
    private val getClientsUseCase: GetClientsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<ProjectDetailsEditUiState> =
        MutableStateFlow(ProjectDetailsEditUiState())
    val state: StateFlow<ProjectDetailsEditUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val saveEventChannel = Channel<Uuid>()
    val saveEventFlow = saveEventChannel.receiveAsFlow()

    init {
        projectId?.let { collectProject(it) }
        collectClients()
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
                            _state.update {
                                it.copy(
                                    projectName = data.project.name,
                                    projectAddress = data.project.address,
                                    selectedClientId = data.project.clientId,
                                    selectedClientName =
                                        data.project.clientId?.let { clientId ->
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
                        _state.update { current ->
                            val resolvedName =
                                if (current.selectedClientId != null && current.selectedClientName.isEmpty()) {
                                    val matchingClient =
                                        clients.firstOrNull { it.client.id == current.selectedClientId }
                                    matchingClient?.client?.name ?: ""
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
            _state.value.availableClients.firstOrNull { it.client.id == clientId }
        return matchingClient?.client?.name ?: ""
    }

    fun onClientSelected(
        clientId: Uuid,
        clientName: String,
    ) {
        _state.update {
            it.copy(
                selectedClientId = clientId,
                selectedClientName = clientName,
            )
        }
    }

    fun onClientCleared() {
        _state.update {
            it.copy(
                selectedClientId = null,
                selectedClientName = "",
            )
        }
    }

    fun onClientCreated(clientId: Uuid) {
        _state.update {
            it.copy(
                selectedClientId = clientId,
                selectedClientName = resolveClientName(clientId),
            )
        }
    }

    fun onProjectNameChange(updatedName: String) {
        _state.update { it.copy(projectName = updatedName) }
    }

    fun onProjectAddressChange(updatedAddress: String) {
        _state.update { it.copy(projectAddress = updatedAddress) }
    }

    fun onSaveProject() =
        viewModelScope.launch {
            if (state.value.projectName.isBlank()) {
                // TODO use string provider
                errorEventsChannel.send(CustomUserMessage("Project name cannot be empty"))
            } else if (state.value.projectAddress.isBlank()) {
                errorEventsChannel.send(CustomUserMessage("Project address cannot be empty"))
            } else {
                saveProject()
            }
        }

    private suspend fun saveProject() {
        val result =
            saveProjectUseCase(
                request =
                    SaveProjectRequest(
                        id = projectId,
                        name = state.value.projectName,
                        address = state.value.projectAddress,
                        clientId = state.value.selectedClientId,
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
