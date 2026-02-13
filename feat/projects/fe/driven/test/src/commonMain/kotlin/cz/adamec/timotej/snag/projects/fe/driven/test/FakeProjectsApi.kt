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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

class FakeProjectsApi : ProjectsApi {
    private val ops = FakeApiOps<FrontendProject, ProjectSyncResult>(getId = { it.project.id })

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

    override suspend fun getProjects(): OnlineDataResult<List<FrontendProject>> = ops.getAllItems()

    override suspend fun getProject(id: Uuid): OnlineDataResult<FrontendProject> = ops.getItemById(id)

    override suspend fun saveProject(project: FrontendProject): OnlineDataResult<FrontendProject?> = ops.saveItem(project)

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> = ops.deleteItemById(id)

    override suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>> = ops.getModifiedSinceItems()

    fun setProject(project: FrontendProject) = ops.setItem(project)
}
