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

package cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

internal class EnqueueSyncSaveUseCaseImpl(
    private val enqueueSyncOperationUseCase: EnqueueSyncOperationUseCase,
) : EnqueueSyncSaveUseCase {
    override suspend fun invoke(
        entityTypeId: String,
        entityId: Uuid,
    ) {
        enqueueSyncOperationUseCase(entityTypeId, entityId, SyncOperationType.UPSERT)
    }
}
