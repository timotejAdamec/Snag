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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import kotlin.uuid.Uuid

sealed interface ProjectSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : ProjectSyncResult

    data class Updated(
        val project: AppProject,
    ) : ProjectSyncResult
}

interface ProjectsApi {
    suspend fun getProjects(): OnlineDataResult<List<AppProject>>

    suspend fun getProject(id: Uuid): OnlineDataResult<AppProject>

    suspend fun saveProject(project: AppProject): OnlineDataResult<AppProject?>

    suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProject?>

    suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>>

    suspend fun getProjectAssignments(projectId: Uuid): OnlineDataResult<Set<Uuid>>

    suspend fun assignUserToProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit>

    suspend fun removeUserFromProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit>
}
