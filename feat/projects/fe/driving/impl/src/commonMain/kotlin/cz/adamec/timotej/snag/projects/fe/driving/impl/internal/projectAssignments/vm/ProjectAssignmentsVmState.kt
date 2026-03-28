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

import cz.adamec.timotej.snag.users.app.model.AppUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.uuid.Uuid

internal data class ProjectAssignmentsVmState(
    val allUsers: ImmutableList<AppUser> = persistentListOf(),
    val assignedUserIds: Set<Uuid> = emptySet(),
    val canManageAssignments: Boolean = false,
    val usersLoaded: Boolean = false,
    val assignmentsLoaded: Boolean = false,
)
