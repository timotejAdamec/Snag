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
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

class FakeProjectsApi : ProjectsApi {
    private val projects = mutableMapOf<Uuid, FrontendProject>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveProjectResponseOverride: ((FrontendProject) -> OnlineDataResult<FrontendProject?>)? = null
    var modifiedSinceResults: List<ProjectSyncResult> = emptyList()

    override suspend fun getProjects(): OnlineDataResult<List<FrontendProject>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(projects.values.toList())
    }

    override suspend fun getProject(id: Uuid): OnlineDataResult<FrontendProject> {
        val failure = forcedFailure
        if (failure != null) return failure
        return projects[id]?.let { OnlineDataResult.Success(it) }
            ?: OnlineDataResult.Failure.ProgrammerError(Exception("Not found"))
    }

    override suspend fun saveProject(project: FrontendProject): OnlineDataResult<FrontendProject?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveProjectResponseOverride
        return if (override != null) {
            override(project)
        } else {
            projects[project.project.id] = project
            OnlineDataResult.Success(project)
        }
    }

    override suspend fun deleteProject(id: Uuid, deletedAt: Timestamp): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        projects.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(modifiedSinceResults)
    }

    fun setProject(project: FrontendProject) {
        projects[project.project.id] = project
    }
}
