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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
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
    private val _state: MutableStateFlow<UserManagementUiState> =
        MutableStateFlow(UserManagementUiState())
    val state: StateFlow<UserManagementUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private var currentUsers: List<FrontendUser> = emptyList()

    init {
        collectUsers()
    }

    private fun collectUsers() =
        getUsersUseCase()
            .map { usersDataResult ->
                when (usersDataResult) {
                    is OfflineFirstDataResult.ProgrammerError -> {
                        _state.update { it.copy(isLoading = false) }
                        errorEventsChannel.send(UiError.Unknown)
                    }
                    is OfflineFirstDataResult.Success -> {
                        currentUsers = usersDataResult.data
                        _state.update {
                            it.copy(
                                users = usersDataResult.data.map { user -> user.toUserItem() }.toPersistentList(),
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
        val user = currentUsers.find { it.user.id == userId } ?: return
        viewModelScope.launch {
            when (changeUserRoleUseCase(user, newRole)) {
                is OnlineDataResult.Success -> {}
                is OnlineDataResult.Failure -> errorEventsChannel.send(UiError.Unknown)
            }
        }
    }
}

private fun FrontendUser.toUserItem() =
    UserItem(
        id = user.id,
        email = user.email,
        role = user.role,
    )
