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

package cz.adamec.timotej.snag.structures.fe.driven.internal.sync

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationResult
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class StructureSyncHandler(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
) : SyncOperationHandler {
    override val entityType: String = STRUCTURE_SYNC_ENTITY_TYPE

    override suspend fun execute(
        entityId: Uuid,
        operationType: SyncOperationType,
    ): SyncOperationResult =
        when (operationType) {
            SyncOperationType.UPSERT -> executeUpsert(entityId)
            SyncOperationType.DELETE -> {
                logger.w { "Delete not yet supported for structures." }
                SyncOperationResult.Failure
            }
        }

    private suspend fun executeUpsert(entityId: Uuid): SyncOperationResult {
        val structureResult = structuresDb.getStructureFlow(entityId).first()
        val structure =
            when (structureResult) {
                is OfflineFirstDataResult.Success -> structureResult.data
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.e { "DB error reading structure $entityId for sync." }
                    return SyncOperationResult.Failure
                }
            }
        if (structure == null) {
            logger.d { "Structure $entityId not found in local DB, discarding sync operation." }
            return SyncOperationResult.EntityNotFound
        }

        return when (val apiResult = structuresApi.saveStructure(structure)) {
            is OnlineDataResult.Success -> {
                apiResult.data?.let { updatedStructure ->
                    logger.d { "Saving fresher $updatedStructure from API to DB." }
                    structuresDb.saveStructure(updatedStructure)
                }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure syncing structure $entityId." }
                SyncOperationResult.Failure
            }
        }
    }
}
