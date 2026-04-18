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

import androidx.compose.runtime.Immutable
import cz.adamec.timotej.snag.authorization.business.UserRole
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.uuid.Uuid

@Immutable
data class ProjectAssignmentsUiState(
    val assignedUsers: ImmutableList<AssignedUserItem> = persistentListOf(),
    val availableUsers: ImmutableList<AssignedUserItem> = persistentListOf(),
    val canManageAssignments: Boolean = false,
    val isLoading: Boolean = true,
)

@Immutable
data class AssignedUserItem(
    val id: Uuid,
    val email: String,
    val role: UserRole?,
)
