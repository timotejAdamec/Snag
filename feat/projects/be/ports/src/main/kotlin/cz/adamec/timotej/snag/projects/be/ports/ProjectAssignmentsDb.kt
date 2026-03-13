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

package cz.adamec.timotej.snag.projects.be.ports

import cz.adamec.timotej.snag.users.be.model.BackendUser
import kotlin.uuid.Uuid

interface ProjectAssignmentsDb {
    suspend fun getAssignedUsers(projectId: Uuid): List<BackendUser>

    suspend fun assignUser(
        userId: Uuid,
        projectId: Uuid,
    )

    suspend fun removeUser(
        userId: Uuid,
        projectId: Uuid,
    )

    suspend fun getProjectsForUser(userId: Uuid): List<Uuid>
}
