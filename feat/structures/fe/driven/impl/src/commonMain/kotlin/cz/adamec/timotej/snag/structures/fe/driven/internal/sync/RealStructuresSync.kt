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

import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncOperationUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import kotlin.uuid.Uuid

internal class RealStructuresSync(
    private val enqueueSyncOperationUseCase: EnqueueSyncOperationUseCase,
) : StructuresSync {
    override suspend fun enqueueStructureSave(structureId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = STRUCTURE_SYNC_ENTITY_TYPE,
            entityId = structureId,
            operationType = SyncOperationType.UPSERT,
        )
    }

    override suspend fun enqueueStructureDelete(structureId: Uuid) {
        enqueueSyncOperationUseCase(
            entityType = STRUCTURE_SYNC_ENTITY_TYPE,
            entityId = structureId,
            operationType = SyncOperationType.DELETE,
        )
    }
}
