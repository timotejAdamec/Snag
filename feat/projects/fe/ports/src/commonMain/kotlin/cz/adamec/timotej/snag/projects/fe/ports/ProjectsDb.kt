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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ProjectsDb {
    fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<AppProject>>>

    suspend fun saveProjects(projects: List<AppProject>): OfflineFirstDataResult<Unit>

    fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProject?>>

    suspend fun getProject(id: Uuid): OfflineFirstDataResult<AppProject?>

    suspend fun saveProject(project: AppProject): OfflineFirstDataResult<Unit>

    suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit>

    suspend fun isClientReferencedByProject(clientId: Uuid): OfflineFirstDataResult<Boolean>
}
