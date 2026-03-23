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

package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ProjectAssignmentsDb {
    fun getAssignedUserIdsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<Set<Uuid>>>

    suspend fun replaceAssignments(
        projectId: Uuid,
        userIds: Set<Uuid>,
    ): OfflineFirstDataResult<Unit>

    suspend fun deleteAssignmentsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit>
}
