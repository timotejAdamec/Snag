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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.sync

import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

internal class RealInspectionsSync(
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : InspectionsSync {
    override suspend fun enqueueInspectionSave(inspectionId: Uuid) {
        enqueueSyncSaveUseCase(INSPECTION_SYNC_ENTITY_TYPE, inspectionId)
    }

    override suspend fun enqueueInspectionDelete(inspectionId: Uuid) {
        enqueueSyncDeleteUseCase(INSPECTION_SYNC_ENTITY_TYPE, inspectionId)
    }
}
