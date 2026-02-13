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

package cz.adamec.timotej.snag.projects.fe.driven.internal.db

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val ops: ProjectsSqlDelightDbOps,
) : ProjectsDb {
    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<FrontendProject>>> = ops.allEntitiesFlow()

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendProject?>> = ops.entityByIdFlow(id)

    override suspend fun saveProject(project: FrontendProject): OfflineFirstDataResult<Unit> = ops.saveOne(project)

    override suspend fun saveProjects(projects: List<FrontendProject>): OfflineFirstDataResult<Unit> = ops.saveMany(projects)

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteById(id)
}
