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

import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.SetLastPullSyncedAtTimestampRequest
import cz.adamec.timotej.snag.sync.fe.ports.PullSyncTimestampDb

internal class SetLastPullSyncedAtTimestampUseCaseImpl(
    private val timestampDb: PullSyncTimestampDb,
) : SetLastPullSyncedAtTimestampUseCase {
    override suspend fun invoke(request: SetLastPullSyncedAtTimestampRequest) {
        timestampDb.setLastSyncedAt(request.entityType, request.scopeId, request.timestamp)
    }
}
