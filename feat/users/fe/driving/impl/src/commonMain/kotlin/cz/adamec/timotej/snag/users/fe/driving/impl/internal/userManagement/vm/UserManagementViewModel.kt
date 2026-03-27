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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.design.fe.error.toUiError
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetAllowedRoleOptionsUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.app.api.model.ChangeUserRoleRequest
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.toUserItem
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class UserManagementViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
    private val getAllowedRoleOptionsUseCase: GetAllowedRoleOptionsUseCase,
) : ViewModel() {
    private val _state: MutableStateFlow<UserManagementUiState> =
        MutableStateFlow(UserManagementUiState())
    val state: StateFlow<UserManagementUiState> = _state

    private val errorEventsChannel = Channel<UiError>()
    val errorsFlow = errorEventsChannel.receiveAsFlow()

    private val roleOptionsMap = MutableStateFlow<Map<UserRole?, Set<UserRole?>>>(emptyMap())

    init {
        collectUsers()
        collectAllowedRoleOptions()
    }

    private fun collectUsers() =
        combine(
            getUsersUseCase(),
            roleOptionsMap,
        ) { usersDataResult, optionsMap ->
            when (usersDataResult) {
                is OfflineFirstDataResult.ProgrammerError -> {
                    _state.update { it.copy(isLoading = false) }
                    errorEventsChannel.send(UiError.Unknown)
                }
                is OfflineFirstDataResult.Success -> {
                    _state.update { currentState ->
                        currentState.copy(
                            users =
                                usersDataResult.data
                                    .map { user ->
                                        val existing = currentState.users.find { it.id == user.id }
                                        user.toUserItem().copy(
                                            isUpdatingRole = existing?.isUpdatingRole ?: false,
                                            allowedRoleOptions = optionsMap[user.role] ?: emptySet(),
                                        )
                                    }.toPersistentList(),
                            isLoading = false,
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)

    private fun collectAllowedRoleOptions() {
        val allCurrentRoles: List<UserRole?> = UserRole.entries + null
        val flows = allCurrentRoles.map { currentRole ->
            getAllowedRoleOptionsUseCase(targetCurrentRole = currentRole)
                .map { options -> currentRole to options }
        }
        combine(flows) { results ->
            results.toMap()
        }.map { optionsMap ->
            roleOptionsMap.value = optionsMap
        }.launchIn(viewModelScope)
    }

    fun onRoleChanged(
        userId: Uuid,
        newRole: UserRole?,
    ) {
        val user = state.value.users.find { it.id == userId } ?: return
        if (newRole !in user.allowedRoleOptions) return
        viewModelScope.launch {
            updateUserItem(userId) { it.copy(isUpdatingRole = true) }
            when (val result = changeUserRoleUseCase(ChangeUserRoleRequest(userId, newRole))) {
                is OnlineDataResult.Success -> {
                    updateUserItem(userId) { it.copy(isUpdatingRole = false) }
                }
                is OnlineDataResult.Failure -> {
                    updateUserItem(userId) { it.copy(isUpdatingRole = false) }
                    errorEventsChannel.send(result.toUiError())
                }
            }
        }
    }

    private fun updateUserItem(
        userId: Uuid,
        transform: (UserItem) -> UserItem,
    ) {
        _state.update { state ->
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
