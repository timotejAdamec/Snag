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

package cz.adamec.timotej.snag.lib.sync.fe.app.internal

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncOperationUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationResult
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncQueue
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

internal class SyncEngine(
    private val syncQueue: SyncQueue,
    private val handlers: List<SyncOperationHandler>,
    private val applicationScope: ApplicationScope,
) : EnqueueSyncOperationUseCase {
    private val mutex = Mutex()

    override suspend fun invoke(
        entityType: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    ) {
        require(handlers.any { it.entityType == entityType }) {
            "No SyncOperationHandler registered for entityType='$entityType'"
        }
        syncQueue.enqueue(entityType, entityId, operationType)
        applicationScope.launch {
            processAll()
        }
    }

    private suspend fun processAll() {
        mutex.withLock {
            val pending = syncQueue.getAllPending()
            for (operation in pending) {
                val handler =
                    handlers.find { it.entityType == operation.entityType }
                        ?: error(
                            "No SyncOperationHandler registered for entityType='${operation.entityType}'",
                        )
                when (handler.execute(operation.entityId, operation.operationType)) {
                    SyncOperationResult.Success -> {
                        LH.logger.d { "Sync operation ${operation.id} succeeded, removing from queue." }
                        syncQueue.remove(operation.id)
                    }
                    SyncOperationResult.EntityNotFound -> {
                        LH.logger.d { "Entity not found for operation ${operation.id}, discarding." }
                        syncQueue.remove(operation.id)
                    }
                    SyncOperationResult.Failure -> {
                        LH.logger.w { "Sync operation ${operation.id} failed, stopping processing." }
                        return
                    }
                }
            }
        }
    }
}
