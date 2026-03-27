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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.mapState
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.toUiError
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.app.api.model.ChangeUserRoleRequest
import cz.adamec.timotej.snag.lib.design.fe.state.launchWhileSubscribed
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.toUserVmItem
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class UserManagementViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
) : ViewModel() {
    private val vmState: MutableStateFlow<UserManagementVmState> =
        MutableStateFlow(UserManagementVmState())
            .launchWhileSubscribed(scope = viewModelScope) {
                listOf(collectUsers())
            }
    val state: StateFlow<UserManagementUiState> =
        vmState.mapState { it.toUiState() }

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private fun collectUsers() =
        getUsersUseCase()
            .map { usersDataResult ->
                when (usersDataResult) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        vmState.update { it.copy(isLoading = false) }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        vmState.update { currentState ->
                            currentState.copy(
                                users =
                                    usersDataResult.data
                                        .map { user ->
                                            val existing = currentState.users.find { it.id == user.id }
                                            user.toUserVmItem().copy(
                                                isUpdatingRole = existing?.isUpdatingRole ?: false,
                                            )
                                        }.toPersistentList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)

    fun onRoleChanged(
        userId: Uuid,
        newRole: UserRole?,
    ) {
        viewModelScope.launch {
            updateUserVmItem(userId) { it.copy(isUpdatingRole = true) }
            when (val result = changeUserRoleUseCase(ChangeUserRoleRequest(userId, newRole))) {
                is OnlineDataResult.Success -> {
                    updateUserVmItem(userId) { it.copy(isUpdatingRole = false) }
                }
                is OnlineDataResult.Failure -> {
                    updateUserVmItem(userId) { it.copy(isUpdatingRole = false) }
                    errorEventsChannel.send(result.toUiError())
                }
            }
        }
    }

    private fun updateUserVmItem(
        userId: Uuid,
        transform: (UserVmItem) -> UserVmItem,
    ) {
        vmState.update { state ->
            state.copy(
                users =
                    state.users
                        .map { item ->
                            if (item.id == userId) transform(item) else item
                        }.toPersistentList(),
            )
        }
    }
}
