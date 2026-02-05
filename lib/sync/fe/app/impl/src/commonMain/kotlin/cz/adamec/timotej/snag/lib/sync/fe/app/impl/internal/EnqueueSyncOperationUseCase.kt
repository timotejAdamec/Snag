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

import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import kotlin.uuid.Uuid

internal interface EnqueueSyncOperationUseCase {
    suspend operator fun invoke(
        entityTypeId: String,
        entityId: Uuid,
        operationType: SyncOperationType,
    )
}
