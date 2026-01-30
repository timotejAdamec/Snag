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

/**
 * Make sure all [cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler]s are implemented for all [SyncOperationType]s you invoke
 * [EnqueueSyncOperationUseCase] with.
 */
interface EnqueueSyncOperationUseCase {
    /**
     * @throws IllegalArgumentException if [cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler] is not registered for given [operationType].
     */
    suspend operator fun invoke(
        entityType: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    )
}
