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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.GetProjectsUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class ProjectsViewModel(
    private val getProjectsUseCase: GetProjectsUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ProjectsUiState> = MutableStateFlow(ProjectsUiState())
    val state: StateFlow<ProjectsUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    init {
        collectProjects()
    }

    private fun collectProjects() =
        getProjectsUseCase()
            .map { projectsDataResult ->
                when (projectsDataResult) {
                    DataResult.Failure.NetworkUnavailable ->
                        _state.update {
                            it.copy(
                                error = UiError.NetworkUnavailable,
                                isLoading = false,
                            )
                        }

                    is DataResult.Failure.ProgrammerError ->
                        _state.update {
                            it.copy(
                                error = UiError.Unknown,
                                isLoading = false,
                            )
                        }

                    is DataResult.Failure.UserMessageError ->
                        _state.update {
                            it.copy(
                                error = UiError.CustomUserMessage(projectsDataResult.message),
                                isLoading = false,
                            )
                        }

                    DataResult.Loading ->
                        _state.update {
                            it.copy(
                                isLoading = true,
                            )
                        }

                    is DataResult.Success ->
                        _state.update {
                            it.copy(
                                projects = projectsDataResult.data.toPersistentList(),
                                isLoading = false,
                            )
                        }
                }
            }.launchIn(viewModelScope)

    internal fun onErrorMessageShown() {
        _state.update {
            it.copy(
                error = null,
            )
        }
    }
}
