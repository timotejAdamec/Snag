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
import cz.adamec.timotej.snag.lib.core.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.CustomUserMessage
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.Unknown
import cz.adamec.timotej.snag.projects.fe.app.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.model.SaveProjectRequest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class ProjectDetailsEditViewModel(
    private val projectId: Uuid?,
    private val getProjectUseCase: GetProjectUseCase,
    private val saveProjectUseCase: SaveProjectUseCase,
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
    }

    private fun collectProject(projectId: Uuid) = viewModelScope.launch {
        getProjectUseCase(projectId).collect { result ->
            when (result) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    errorEventsChannel.send(Unknown)
                }
                is OfflineFirstDataResult.Success -> result.data?.let { data ->
                    _state.update {
                        it.copy(
                            projectName = data.name,
                            projectAddress = data.address,
                        )
                    }
                    cancel()
                }
            }
        }
    }

    fun onProjectNameChange(updatedName: String) {
        _state.update { it.copy(projectName = updatedName) }
    }

    fun onProjectAddressChange(updatedAddress: String) {
        _state.update { it.copy(projectAddress = updatedAddress) }
    }

    fun onSaveProject() = viewModelScope.launch {
        if (state.value.projectName.isBlank()) {
            // TODO use string provider
            errorEventsChannel.send(CustomUserMessage("Project name cannot be empty"))
            return@launch
        }
        if (state.value.projectAddress.isBlank()) {
            errorEventsChannel.send(CustomUserMessage("Project address cannot be empty"))
            return@launch
        }

        val result = saveProjectUseCase(
            request = SaveProjectRequest(
                id = projectId,
                name = state.value.projectName,
                address = state.value.projectAddress,
            )
        )
        when (result) {
            is OfflineFirstDataResult.ProgrammerError -> {
                errorEventsChannel.send(Unknown)
            }
            is OfflineFirstDataResult.Success -> {
                saveEventChannel.send(result.data.id)
            }
        }
    }
}
