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

package cz.adamec.timotej.snag.sync.fe.driven.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.sync.fe.driven.impl.db.SyncOperationEntityQueries
import cz.adamec.timotej.snag.sync.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.sync.fe.ports.SyncOperation
import cz.adamec.timotej.snag.sync.fe.ports.SyncQueue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealSyncQueue(
    private val syncOperationEntityQueries: SyncOperationEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
    private val uuidProvider: UuidProvider,
) : SyncQueue {
    override suspend fun enqueue(
        entityTypeId: String,
        entityId: Uuid,
        operationType: SyncOperationType,
        scopeId: Uuid?,
    ) {
        withContext(ioDispatcher) {
            syncOperationEntityQueries.enqueue(
                id = uuidProvider.getUuid().toString(),
                entityType = entityTypeId,
                entityId = entityId.toString(),
                operationType = operationType.name,
                scopeId = scopeId?.toString(),
            )
        }
        logger.d {
            "Enqueued sync operation: entityTypeId=$entityTypeId, entityId=$entityId, " +
                "operationType=$operationType, scopeId=$scopeId"
        }
    }

    override suspend fun getAllPending(): List<SyncOperation> =
        withContext(ioDispatcher) {
            syncOperationEntityQueries.selectAllPending().awaitAsList().map { entity ->
                SyncOperation(
                    id = Uuid.parse(entity.id),
                    entityTypeId = entity.entityType,
                    entityId = Uuid.parse(entity.entityId),
                    operationType = entity.operationType.let { SyncOperationType.valueOf(it) },
                    scopeId = entity.scopeId?.let { Uuid.parse(it) },
                )
            }
        }

    override suspend fun remove(operationId: Uuid) {
        withContext(ioDispatcher) {
            syncOperationEntityQueries.deleteById(operationId.toString())
        }
    }
}
