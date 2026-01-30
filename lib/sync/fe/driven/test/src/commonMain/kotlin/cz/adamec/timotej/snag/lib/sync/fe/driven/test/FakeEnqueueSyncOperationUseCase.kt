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

import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncOperationUseCase
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import kotlin.uuid.Uuid

class FakeEnqueueSyncOperationUseCase : EnqueueSyncOperationUseCase {
    val enqueuedOperations = mutableListOf<EnqueuedOperation>()

    override suspend fun invoke(
        entityType: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    ) {
        enqueuedOperations.add(EnqueuedOperation(entityType, entityId, operationType))
    }

    data class EnqueuedOperation(
        val entityType: String,
        val entityId: Uuid,
        val operationType: SyncOperationType,
    )
}
