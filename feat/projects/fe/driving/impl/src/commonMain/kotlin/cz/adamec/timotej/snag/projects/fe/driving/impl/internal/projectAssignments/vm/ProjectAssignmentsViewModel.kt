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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

internal class ProjectAssignmentsViewModel(
    @InjectedParam private val projectId: Uuid,
    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase,
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase,
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<ProjectAssignmentsVmState> =
        MutableStateFlow(ProjectAssignmentsVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                listOf(
                    collectAssignments(projectId),
                    collectUsers(),
                    collectCanManageAssignments(projectId),
                )
            }
    val state: StateFlow<ProjectAssignmentsUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private fun collectAssignments(projectId: Uuid) =
        viewModelScope.launch {
            getProjectAssignmentsUseCase(projectId).collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update { it.copy(assignmentsLoaded = true) }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                assignedUserIds = result.data,
                                assignmentsLoaded = true,
                            )
                        }
                    }
                }
            }
        }

    private fun collectUsers() =
        viewModelScope.launch {
            getUsersUseCase().collect { result ->
                when (result) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update { it.copy(usersLoaded = true) }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update {
                            it.copy(
                                allUsers = result.data.toImmutableList(),
                                usersLoaded = true,
                            )
                        }
                    }
                }
            }
        }

    private fun collectCanManageAssignments(projectId: Uuid) =
        viewModelScope.launch {
            canAssignUserToProjectUseCase(projectId).collect { canManage ->
                vmState.update { it.copy(canManageAssignments = canManage) }
            }
        }

    fun onAssignUser(userId: Uuid) =
        viewModelScope.launch {
            assignUserToProjectUseCase(
                projectId = projectId,
                userId = userId,
            )
        }

    fun onRemoveUser(userId: Uuid) =
        viewModelScope.launch {
            removeUserFromProjectUseCase(
                projectId = projectId,
                userId = userId,
            )
        }
}
