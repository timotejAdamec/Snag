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

package cz.adamec.timotej.snag.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.sync.fe.app.api.SyncCoordinator
import cz.adamec.timotej.snag.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.sync.fe.ports.SyncOperation
import cz.adamec.timotej.snag.sync.fe.ports.SyncQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

internal class SyncEngine(
    private val syncQueue: SyncQueue,
    private val handlers: List<SyncOperationHandler>,
    private val applicationScope: ApplicationScope,
) : EnqueueSyncOperationUseCase,
    SyncCoordinator {
    private val mutex = Mutex()
    private val _status = MutableStateFlow<SyncEngineStatus>(SyncEngineStatus.Idle)
    val status: StateFlow<SyncEngineStatus> = _status.asStateFlow()

    override suspend fun invoke(
        entityTypeId: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    ) {
        require(handlers.any { it.entityTypeId == entityTypeId }) {
            "No SyncOperationHandler registered for entityTypeId='$entityTypeId'"
        }
        syncQueue.enqueue(entityTypeId, entityId, operationType)
        applicationScope.launch {
            processAll()
        }
    }

    override suspend fun <T> withFlushedQueue(block: suspend (wasFlushingSuccessful: Boolean) -> T): T =
        mutex.withLock {
            val result = processAllPending()
            block(result)
        }

    private suspend fun processAll() {
        mutex.withLock {
            processAllPending()
        }
    }

    private suspend fun processAllPending(): Boolean {
        val pending = syncQueue.getAllPending()
        if (pending.isEmpty()) {
            _status.value = SyncEngineStatus.Idle
            return true
        }
        _status.value = SyncEngineStatus.Syncing
        val succeeded = processPendingOperations(pending)
        _status.value = if (succeeded) SyncEngineStatus.Idle else SyncEngineStatus.Failed
        return succeeded
    }

    private suspend fun processPendingOperations(pending: List<SyncOperation>): Boolean {
        for (operation in pending) {
            val handler =
                handlers.find { it.entityTypeId == operation.entityTypeId }
                    ?: error(
                        "No SyncOperationHandler registered for entityTypeId='${operation.entityTypeId}'",
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
                    return false
                }
            }
        }
        return true
    }
}
