package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

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

class FakeProjectsRepository : ProjectsRepository {
    private val projects = MutableStateFlow<Map<Uuid, Project>>(emptyMap())
    private var saveResult: DataResult<Project>? = null

    fun setProject(id: Uuid, project: Project) {
        projects.update { it + (id to project) }
    }

    fun setSaveResult(result: DataResult<Project>) {
        saveResult = result
    }

    override fun getAllProjectsFlow(): Flow<DataResult<List<Project>>> =
        projects.map { DataResult.Success(it.values.toList()) }

    override fun getProjectFlow(id: Uuid): Flow<DataResult<Project?>> =
        projects.map { DataResult.Success(it[id]) }

    override suspend fun saveProject(project: Project): DataResult<Project> {
        val result = saveResult ?: DataResult.Success(project)
        if (result is DataResult.Success) {
            setProject(result.data.id, result.data)
        }
        return result
    }
}