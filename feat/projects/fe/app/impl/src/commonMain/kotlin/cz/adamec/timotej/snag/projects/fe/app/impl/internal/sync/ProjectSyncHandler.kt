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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeRestoreLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeRestoreLocalStructuresByProjectIdUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class ProjectSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val cascadeRestoreLocalStructuresByProjectIdUseCase: CascadeRestoreLocalStructuresByProjectIdUseCase,
    private val cascadeRestoreLocalInspectionsByProjectIdUseCase: CascadeRestoreLocalInspectionsByProjectIdUseCase,
    timestampProvider: TimestampProvider,
) : DbApiSyncHandler<AppProject>(LH.logger, timestampProvider) {
    override val entityTypeId: String = PROJECT_SYNC_ENTITY_TYPE
    override val entityName: String = "project"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppProject?>> = projectsDb.getProjectFlow(entityId)

    override suspend fun saveEntityToApi(entity: AppProject): OnlineDataResult<AppProject?> = projectsApi.saveProject(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProject?> = projectsApi.deleteProject(entityId, deletedAt)

    override suspend fun saveEntityToDb(entity: AppProject): OfflineFirstDataResult<Unit> = projectsDb.saveProject(entity)

    override suspend fun onDeleteRejected(entityId: Uuid) {
        coroutineScope {
            launch { cascadeRestoreLocalStructuresByProjectIdUseCase(entityId) }
            launch { cascadeRestoreLocalInspectionsByProjectIdUseCase(entityId) }
        }
    }
}
