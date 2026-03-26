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

import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType

internal class EnqueueSyncSaveUseCaseImpl(
    private val enqueueSyncOperationUseCase: EnqueueSyncOperationUseCase,
) : EnqueueSyncSaveUseCase {
    override suspend fun invoke(request: EnqueueSyncSaveRequest) {
        enqueueSyncOperationUseCase(
            entityTypeId = request.entityTypeId,
            entityId = request.entityId,
            operationType = SyncOperationType.UPSERT,
            scopeId = request.scopeId,
        )
    }
}
