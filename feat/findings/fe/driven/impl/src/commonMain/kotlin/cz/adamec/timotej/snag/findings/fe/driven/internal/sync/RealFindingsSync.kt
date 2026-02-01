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

import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncOperationUseCase
import kotlin.uuid.Uuid

internal class RealFindingsSync(
    private val enqueueSyncOperationUseCase: EnqueueSyncOperationUseCase,
) : FindingsSync {
    override suspend fun enqueueFindingSave(findingId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = FINDING_SYNC_ENTITY_TYPE,
            entityId = findingId,
            operationType = SyncOperationType.UPSERT,
        )
    }

    override suspend fun enqueueFindingDelete(findingId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = FINDING_SYNC_ENTITY_TYPE,
            entityId = findingId,
            operationType = SyncOperationType.DELETE,
        )
    }
}
