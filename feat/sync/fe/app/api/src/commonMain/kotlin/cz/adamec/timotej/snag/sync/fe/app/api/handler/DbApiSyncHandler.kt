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

package cz.adamec.timotej.snag.sync.fe.app.api.handler

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

abstract class DbApiSyncHandler<T>(
    private val logger: Logger,
    private val timestampProvider: TimestampProvider,
) : SyncOperationHandler {
    /**
     * Freeform string used for logging.
     */
    protected abstract val entityName: String

    protected abstract fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<T?>>

    protected abstract suspend fun saveEntityToApi(entity: T): OnlineDataResult<T?>

    protected abstract suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<T?>

    protected abstract suspend fun saveEntityToDb(entity: T): OfflineFirstDataResult<Unit>

    protected open suspend fun onDeleteRejected(entityId: Uuid) {}

    override suspend fun execute(
        entityId: Uuid,
        operationType: SyncOperationType,
    ): SyncOperationResult =
        when (operationType) {
            SyncOperationType.UPSERT -> executeUpsert(entityId)
            SyncOperationType.DELETE -> executeDelete(entityId)
        }

    @Suppress("ReturnCount")
    private suspend fun executeUpsert(entityId: Uuid): SyncOperationResult {
        val entityResult = getEntityFlow(entityId).first()
        val entity =
            when (entityResult) {
                is OfflineFirstDataResult.Success -> {
                    entityResult.data
                }
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.e(throwable = entityResult.throwable) {
                        "DB error reading $entityName $entityId for sync. Error: ${entityResult.throwable}"
                    }
                    return SyncOperationResult.Failure
                }
            }
        if (entity == null) {
            logger.d {
                "${entityName.replaceFirstChar { it.uppercase() }} $entityId not found in local DB, discarding sync operation."
            }
            return SyncOperationResult.EntityNotFound
        }

        return when (val apiResult = saveEntityToApi(entity)) {
            is OnlineDataResult.Success -> {
                apiResult.data?.let { updatedEntity ->
                    logger.d { "Saving fresher $updatedEntity from API to DB." }
                    saveEntityToDb(updatedEntity)
                }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure syncing $entityName $entityId." }
                SyncOperationResult.Failure
            }
        }
    }

    private suspend fun executeDelete(entityId: Uuid): SyncOperationResult =
        when (val result = deleteEntityFromApi(entityId, timestampProvider.getNowTimestamp())) {
            is OnlineDataResult.Success -> {
                result.data?.let { updatedEntity ->
                    logger.d { "Delete of $entityName $entityId rejected by API. Saving fresher $updatedEntity to DB." }
                    saveEntityToDb(updatedEntity)
                    onDeleteRejected(entityId)
                } ?: logger.d { "Deleted $entityName $entityId from API." }
                SyncOperationResult.Success
            }
            is OnlineDataResult.Failure -> {
                logger.w { "API failure deleting $entityName $entityId." }
                SyncOperationResult.Failure
            }
        }
}
