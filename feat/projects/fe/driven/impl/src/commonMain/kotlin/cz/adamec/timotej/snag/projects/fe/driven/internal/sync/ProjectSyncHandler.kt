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

package cz.adamec.timotej.snag.projects.fe.driven.internal.sync

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class ProjectSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) : DbApiSyncHandler<Project>(LH.logger) {
    override val entityTypeId: String = PROJECT_SYNC_ENTITY_TYPE
    override val entityName: String = "project"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<Project?>> =
        projectsDb.getProjectFlow(entityId)

    override suspend fun saveEntityToApi(entity: Project): OnlineDataResult<Project?> =
        projectsApi.saveProject(entity)

    override suspend fun deleteEntityFromApi(entityId: Uuid): OnlineDataResult<Unit> =
        projectsApi.deleteProject(entityId)

    override suspend fun saveEntityToDb(entity: Project): OfflineFirstDataResult<Unit> =
        projectsDb.saveProject(entity)
}
