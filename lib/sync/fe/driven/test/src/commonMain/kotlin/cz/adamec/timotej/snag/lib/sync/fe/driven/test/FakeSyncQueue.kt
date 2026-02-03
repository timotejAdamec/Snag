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

package cz.adamec.timotej.snag.lib.sync.fe.driven.test

import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncOperation
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncQueue
import kotlin.uuid.Uuid

class FakeSyncQueue : SyncQueue {
    private val operations = mutableListOf<SyncOperation>()

    override suspend fun enqueue(
        entityTypeId: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    ) {
        val existing = operations.indexOfFirst { it.entityTypeId == entityTypeId && it.entityId == entityId }
        if (existing >= 0) {
            operations[existing] = operations[existing].copy(operationType = operationType)
        } else {
            operations.add(
                SyncOperation(
                    id = Uuid.random(),
                    entityTypeId = entityTypeId,
                    entityId = entityId,
                    operationType = operationType,
                ),
            )
        }
    }

    override suspend fun getAllPending(): List<SyncOperation> = operations.toList()

    override suspend fun remove(operationId: Uuid) {
        operations.removeAll { it.id == operationId }
    }
}
