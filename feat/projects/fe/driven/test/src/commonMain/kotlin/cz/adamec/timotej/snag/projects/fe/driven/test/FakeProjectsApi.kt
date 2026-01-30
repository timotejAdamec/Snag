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

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

class FakeProjectsApi : ProjectsApi {
    private val projects = mutableMapOf<Uuid, Project>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveProjectResponseOverride: ((Project) -> OnlineDataResult<Project?>)? = null

    override suspend fun getProjects(): OnlineDataResult<List<Project>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(projects.values.toList())
    }

    override suspend fun getProject(id: Uuid): OnlineDataResult<Project> {
        val failure = forcedFailure
        if (failure != null) return failure
        val project = projects[id] ?: return OnlineDataResult.Failure.ProgrammerError(Exception("Not found"))
        return OnlineDataResult.Success(project)
    }

    override suspend fun saveProject(project: Project): OnlineDataResult<Project?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveProjectResponseOverride
        if (override != null) return override(project)
        projects[project.id] = project
        return OnlineDataResult.Success(project)
    }

    override suspend fun deleteProject(id: Uuid): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        projects.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    fun setProject(project: Project) {
        projects[project.id] = project
    }
}
