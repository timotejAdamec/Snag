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

package cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectAssignments.vm

import cz.adamec.timotej.snag.users.app.model.AppUser
import kotlinx.collections.immutable.toPersistentList

internal fun ProjectAssignmentsVmState.toUiState(): ProjectAssignmentsUiState {
    val assignedUsers =
        allUsers
            .filter { it.id in assignedUserIds }
            .map { it.toAssignedUserItem() }
            .toPersistentList()
    val availableUsers =
        allUsers
            .filter { it.id !in assignedUserIds }
            .map { it.toAssignedUserItem() }
            .toPersistentList()
    return ProjectAssignmentsUiState(
        assignedUsers = assignedUsers,
        availableUsers = availableUsers,
        canManageAssignments = canManageAssignments,
        isLoading = !usersLoaded || !assignmentsLoaded,
    )
}

internal fun AppUser.toAssignedUserItem() =
    AssignedUserItem(
        id = id,
        email = email,
        role = role,
    )
