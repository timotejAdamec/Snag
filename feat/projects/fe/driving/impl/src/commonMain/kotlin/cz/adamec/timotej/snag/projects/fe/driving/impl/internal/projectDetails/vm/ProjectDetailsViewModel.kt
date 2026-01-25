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
import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.UiError.*
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.GetProjectUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class ProjectDetailsViewModel(
    projectId: Uuid,
    private val getProjectUseCase: GetProjectUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ProjectDetailsUiState> =
        MutableStateFlow(ProjectDetailsUiState())
    val state: StateFlow<ProjectDetailsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    init {
        collectProject(projectId)
    }

    private fun collectProject(projectId: Uuid) = viewModelScope.launch {
        getProjectUseCase(projectId).collect { result ->
            when (result) {
                DataResult.Failure.NetworkUnavailable -> {}
                is DataResult.Failure.ProgrammerError -> {
                    errorEventsChannel.send(Unknown)
                }
                is DataResult.Failure.UserMessageError -> {
                    errorEventsChannel.send(CustomUserMessage(result.message))
                }
                DataResult.Loading -> _state.update { it.copy(status = ProjectDetailsUiStatus.LOADING) }
                is DataResult.Success<Project?> -> result.data?.let {
                    _state.update {
                        it.copy(
                            status = ProjectDetailsUiStatus.LOADED,
                            name = it.name,
                            address = it.address,
                        )
                    }
                } ?: _state.update { it.copy(status = ProjectDetailsUiStatus.NOT_FOUND) }
            }
        }
    }
}
