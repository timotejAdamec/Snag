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
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.GetProjectUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class ProjectDetailsViewModel(
    private val projectId: Uuid,
    private val getProjectUseCase: GetProjectUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ProjectDetailsUiState> =
        MutableStateFlow(ProjectDetailsUiState())
    val state: StateFlow<ProjectDetailsUiState> = _state

    init {
        collectProject()
    }

    private fun collectProject() = viewModelScope.launch {
        getProjectUseCase(projectId).collect { result ->
            when (result) {
                DataResult.Failure.NetworkUnavailable -> TODO()
                is DataResult.Failure.ProgrammerError -> TODO()
                is DataResult.Failure.UserMessageError -> TODO()
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
