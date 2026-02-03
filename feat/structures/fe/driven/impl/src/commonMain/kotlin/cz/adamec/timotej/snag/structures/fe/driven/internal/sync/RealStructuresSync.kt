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

import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import kotlin.uuid.Uuid

internal class RealStructuresSync(
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : StructuresSync {
    override suspend fun enqueueStructureSave(structureId: Uuid) {
        enqueueSyncSaveUseCase(STRUCTURE_SYNC_ENTITY_TYPE, structureId)
    }

    override suspend fun enqueueStructureDelete(structureId: Uuid) {
        enqueueSyncDeleteUseCase(STRUCTURE_SYNC_ENTITY_TYPE, structureId)
    }
}
