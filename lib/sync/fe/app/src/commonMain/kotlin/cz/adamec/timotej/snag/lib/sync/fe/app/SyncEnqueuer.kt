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

package cz.adamec.timotej.snag.lib.sync.fe.app

import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import kotlin.uuid.Uuid

class SyncEnqueuer(
    private val enqueueSyncOperationUseCase: EnqueueSyncOperationUseCase,
    private val entityType: String,
) {
    suspend fun enqueueSave(entityId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = entityType,
            entityId = entityId,
            operationType = SyncOperationType.UPSERT,
        )
    }

    suspend fun enqueueDelete(entityId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = entityType,
            entityId = entityId,
            operationType = SyncOperationType.DELETE,
        )
    }
}
