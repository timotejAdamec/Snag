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
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationResult
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class ProjectSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) : SyncOperationHandler {
    override val entityType: String = PROJECT_SYNC_ENTITY_TYPE

    override suspend fun execute(
        entityId: Uuid,
        operationType: SyncOperationType,
    ): SyncOperationResult =
        when (operationType) {
            SyncOperationType.UPSERT -> executeUpsert(entityId)
            SyncOperationType.DELETE -> executeDelete(entityId)
        }

    private suspend fun executeUpsert(entityId: Uuid): SyncOperationResult {
        val projectResult = projectsDb.getProjectFlow(entityId).first()
        val project =
            when (projectResult) {
                is OfflineFirstDataResult.Success -> projectResult.data
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.e { "DB error reading project $entityId for sync." }
                    return SyncOperationResult.Failure
                }
            }
        if (project == null) {
            logger.d { "Project $entityId not found in local DB, discarding sync operation." }
            return SyncOperationResult.EntityNotFound
        }

        return when (val apiResult = projectsApi.saveProject(project)) {
            is OnlineDataResult.Success -> {
                apiResult.data?.let { updatedProject ->
                    logger.d { "Saving fresher $updatedProject from API to DB." }
                    projectsDb.saveProject(updatedProject)
                }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure syncing project $entityId." }
                SyncOperationResult.Failure
            }
        }
    }

    private suspend fun executeDelete(entityId: Uuid): SyncOperationResult =
        when (projectsApi.deleteProject(entityId)) {
            is OnlineDataResult.Success -> {
                logger.d { "Deleted project $entityId from API." }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure deleting project $entityId." }
                SyncOperationResult.Failure
            }
        }
}
