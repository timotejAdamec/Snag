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

import kotlinx.collections.immutable.toPersistentList

internal fun UserManagementVmState.toUiState(): UserManagementUiState =
    UserManagementUiState(
        users = users.map { it.toUiItem() }.toPersistentList(),
        isLoading = isLoading,
    )

private fun UserVmItem.toUiItem(): UserItem =
    UserItem(
        id = id,
        email = email,
        role = role,
        isRoleChangeEnabled = !isUpdatingRole && allowedRoleOptions.isNotEmpty(),
        allowedRoleOptions = allowedRoleOptions,
    )
