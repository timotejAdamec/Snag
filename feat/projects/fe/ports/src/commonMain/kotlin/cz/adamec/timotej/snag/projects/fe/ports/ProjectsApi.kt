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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import kotlin.uuid.Uuid

sealed interface ProjectSyncResult {
    data class Deleted(val id: Uuid) : ProjectSyncResult
    data class Updated(val project: FrontendProject) : ProjectSyncResult
}

interface ProjectsApi {
    suspend fun getProjects(): OnlineDataResult<List<FrontendProject>>

    suspend fun getProject(id: Uuid): OnlineDataResult<FrontendProject>

    suspend fun saveProject(project: FrontendProject): OnlineDataResult<FrontendProject?>

    suspend fun deleteProject(id: Uuid, deletedAt: Timestamp): OnlineDataResult<Unit>

    suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>>
}
