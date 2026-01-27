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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeProjectsDb : ProjectsDb {
    private val projects = MutableStateFlow<Map<Uuid, Project>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> =
        projects.map { OfflineFirstDataResult.Success(it.values.toList()) }

    override suspend fun saveProjects(projects: List<Project>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        
        this.projects.update { current ->
            current + projects.associateBy { it.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<Project?>> =
        projects.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    override suspend fun saveProject(project: Project): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        projects.update { it + (project.id to project) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        projects.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setProject(project: Project) {
        projects.update { it + (project.id to project) }
    }
}