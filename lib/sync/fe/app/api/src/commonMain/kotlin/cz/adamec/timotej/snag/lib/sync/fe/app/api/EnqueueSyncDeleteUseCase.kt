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

package cz.adamec.timotej.snag.lib.sync.fe.app.api

import kotlin.uuid.Uuid

/**
 * Enqueues a delete sync operation for the given entity.
 *
 * Make sure a [cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler] is registered
 * for the given [entityTypeId].
 */
interface EnqueueSyncDeleteUseCase {
    /**
     * @throws IllegalArgumentException if [cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler]
     * is not registered for given [entityTypeId].
     */
    suspend operator fun invoke(
        entityTypeId: String,
        entityId: Uuid,
    )
}
