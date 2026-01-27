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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.*
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.GetProjectUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class ProjectDetailsViewModel(
    private val projectId: Uuid,
    private val getProjectUseCase: GetProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ProjectDetailsUiState> =
        MutableStateFlow(ProjectDetailsUiState())
    val state: StateFlow<ProjectDetailsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val deletedSuccessfullyEventChannel = Channel<Unit>()
    val deletedSuccessfullyEventFlow = deletedSuccessfullyEventChannel.receiveAsFlow()

    init {
        collectProject(projectId)
    }

    private fun collectProject(projectId: Uuid) = viewModelScope.launch {
        getProjectUseCase(projectId).collect { result ->
            when (result) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    errorEventsChannel.send(Unknown)
                }
                is OfflineFirstDataResult.Success<Project?> -> result.data?.let { project ->
                    _state.update {
                        it.copy(
                            status = ProjectDetailsUiStatus.LOADED,
                            name = project.name,
                            address = project.address,
                        )
                    }
                } ?: if (state.value.status != ProjectDetailsUiStatus.DELETED) {
                    _state.update {
                        it.copy(status = ProjectDetailsUiStatus.NOT_FOUND)
                    }
                } else {
                    // keep as deleted
                }
            }
        }
    }

    fun onDelete() = viewModelScope.launch {
        _state.update {
            it.copy(isBeingDeleted = true)
        }
        when (deleteProjectUseCase(projectId)) {
            is OfflineFirstDataResult.ProgrammerError -> {
                _state.update {
                    it.copy(
                        isBeingDeleted = false
                    )
                }
                errorEventsChannel.send(Unknown)
            }

            is OfflineFirstDataResult.Success -> {
                _state.update {
                    it.copy(
                        status = ProjectDetailsUiStatus.DELETED,
                        isBeingDeleted = false,
                    )
                }
                deletedSuccessfullyEventChannel.send(Unit)
            }
        }
    }
}
