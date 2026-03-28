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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

class FakeProjectsApi : ProjectsApi {
    private val ops = FakeApiOps<AppProject, ProjectSyncResult>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var saveProjectResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    var projectAssignments: MutableMap<Uuid, Set<Uuid>> = mutableMapOf()

    override suspend fun getProjects(): OnlineDataResult<List<AppProject>> = ops.getAllItems()

    override suspend fun getProject(id: Uuid): OnlineDataResult<AppProject> = ops.getItemById(id)

    override suspend fun saveProject(project: AppProject): OnlineDataResult<AppProject?> = ops.saveItem(project)

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProject?> = ops.deleteItemById(id)

    override suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>> = ops.getModifiedSinceItems()

    override suspend fun getProjectAssignments(projectId: Uuid): OnlineDataResult<Set<Uuid>> {
        forcedFailure?.let { return it }
        return OnlineDataResult.Success(projectAssignments[projectId] ?: emptySet())
    }

    override suspend fun assignUserToProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit> {
        forcedFailure?.let { return it }
        projectAssignments[projectId] = (projectAssignments[projectId] ?: emptySet()) + userId
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun removeUserFromProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit> {
        forcedFailure?.let { return it }
        projectAssignments[projectId] = (projectAssignments[projectId] ?: emptySet()) - userId
        return OnlineDataResult.Success(Unit)
    }

    fun setProject(project: AppProject) = ops.setItem(project)
}
