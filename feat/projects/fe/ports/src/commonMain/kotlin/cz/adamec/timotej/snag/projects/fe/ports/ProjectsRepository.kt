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

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ProjectsRepository {
    fun getAllProjectsFlow(): Flow<DataResult<List<Project>>>

    fun getProjectFlow(id: Uuid): Flow<DataResult<Project?>>

    suspend fun saveProject(project: Project): DataResult<Unit>
}
