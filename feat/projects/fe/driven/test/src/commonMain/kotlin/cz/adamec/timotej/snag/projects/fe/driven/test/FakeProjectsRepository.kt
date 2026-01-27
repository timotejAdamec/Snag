package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.lib.core.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.business.Project
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
    private var saveResult: OfflineFirstDataResult<Project>? = null
    private var deleteResult: OfflineFirstDataResult<Unit>? = null

    fun setProject(
        id: Uuid,
        project: Project,
    ) {
        projects.update { it + (id to project) }
    }

    fun setSaveResult(result: OfflineFirstDataResult<Project>) {
        saveResult = result
    }

    fun setDeleteResult(result: OfflineFirstDataResult<Unit>) {
        deleteResult = result
    }

    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> =
        projects.map { OfflineFirstDataResult.Success(it.values.toList()) }

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<Project?>> = projects.map { OfflineFirstDataResult.Success(it[id]) }

    override suspend fun saveProject(project: Project): OfflineFirstDataResult<Project> {
        val result = saveResult ?: OfflineFirstDataResult.Success(project)
        if (result is OfflineFirstDataResult.Success) {
            setProject(result.data.id, result.data)
        }
        return result
    }

    override suspend fun deleteProject(projectId: Uuid): OfflineFirstDataResult<Unit> {
        val result = deleteResult ?: OfflineFirstDataResult.Success(Unit)
        if (result is OfflineFirstDataResult.Success) {
            projects.update { it - projectId }
        }
        return result
    }
}
