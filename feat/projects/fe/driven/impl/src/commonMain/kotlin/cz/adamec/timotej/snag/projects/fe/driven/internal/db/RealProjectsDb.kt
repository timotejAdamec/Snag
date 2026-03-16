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
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val ops: ProjectsSqlDelightDbOps,
) : ProjectsDb {
    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<AppProject>>> = ops.allEntitiesFlow()

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProject?>> = ops.entityByIdFlow(id)

    override suspend fun getProject(id: Uuid): OfflineFirstDataResult<AppProject?> = ops.entityByIdFlow(id).first()

    override suspend fun saveProject(project: AppProject): OfflineFirstDataResult<Unit> = ops.saveOne(project)

    override suspend fun saveProjects(projects: List<AppProject>): OfflineFirstDataResult<Unit> = ops.saveMany(projects)

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteById(id)
}
