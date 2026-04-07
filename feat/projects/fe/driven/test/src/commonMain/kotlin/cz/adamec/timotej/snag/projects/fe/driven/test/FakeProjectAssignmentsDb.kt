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

package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

class FakeProjectAssignmentsDb : ProjectAssignmentsDb {
    private val assignments = MutableStateFlow<Map<Uuid, Set<Uuid>>>(emptyMap())

    override fun getAssignedUserIdsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<Set<Uuid>>> =
        assignments.map { map ->
            OfflineFirstDataResult.Success(map[projectId] ?: emptySet())
        }

    override suspend fun replaceAssignments(
        projectId: Uuid,
        userIds: Set<Uuid>,
    ): OfflineFirstDataResult<Unit> {
        assignments.value = assignments.value + (projectId to userIds)
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteAssignmentsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> {
        assignments.value = assignments.value - projectId
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun getProjectIdsForAssignedUser(userId: Uuid): Set<Uuid> =
        assignments.value
            .filter { (_, userIds) -> userId in userIds }
            .keys

    fun setAssignments(
        projectId: Uuid,
        userIds: Set<Uuid>,
    ) {
        assignments.value = assignments.value + (projectId to userIds)
    }
}
