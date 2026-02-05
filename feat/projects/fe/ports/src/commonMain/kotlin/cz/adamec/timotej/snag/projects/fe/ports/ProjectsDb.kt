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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ProjectsDb {
    fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<FrontendProject>>>

    suspend fun saveProjects(projects: List<FrontendProject>): OfflineFirstDataResult<Unit>

    fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendProject?>>

    suspend fun saveProject(project: FrontendProject): OfflineFirstDataResult<Unit>

    suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit>
}
