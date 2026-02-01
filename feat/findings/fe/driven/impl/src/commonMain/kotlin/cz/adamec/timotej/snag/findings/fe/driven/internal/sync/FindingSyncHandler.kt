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

package cz.adamec.timotej.snag.findings.fe.driven.internal.sync

import cz.adamec.timotej.snag.findings.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationResult
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class FindingSyncHandler(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
) : SyncOperationHandler {
    override val entityType: String = FINDING_SYNC_ENTITY_TYPE

    override suspend fun execute(
        entityId: Uuid,
        operationType: SyncOperationType,
    ): SyncOperationResult =
        when (operationType) {
            SyncOperationType.UPSERT -> executeUpsert(entityId)
            SyncOperationType.DELETE -> executeDelete(entityId)
        }

    private suspend fun executeUpsert(entityId: Uuid): SyncOperationResult {
        val findingResult = findingsDb.getFindingFlow(entityId).first()
        val finding =
            when (findingResult) {
                is OfflineFirstDataResult.Success -> findingResult.data
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.e { "DB error reading finding $entityId for sync." }
                    return SyncOperationResult.Failure
                }
            }
        if (finding == null) {
            logger.d { "Finding $entityId not found in local DB, discarding sync operation." }
            return SyncOperationResult.EntityNotFound
        }

        return when (val apiResult = findingsApi.saveFinding(finding)) {
            is OnlineDataResult.Success -> {
                apiResult.data?.let { updatedFinding ->
                    logger.d { "Saving fresher $updatedFinding from API to DB." }
                    findingsDb.saveFinding(updatedFinding)
                }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure syncing finding $entityId." }
                SyncOperationResult.Failure
            }
        }
    }

    private suspend fun executeDelete(entityId: Uuid): SyncOperationResult =
        when (findingsApi.deleteFinding(entityId)) {
            is OnlineDataResult.Success -> {
                logger.d { "Deleted finding $entityId from API." }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure deleting finding $entityId." }
                SyncOperationResult.Failure
            }
        }
}
