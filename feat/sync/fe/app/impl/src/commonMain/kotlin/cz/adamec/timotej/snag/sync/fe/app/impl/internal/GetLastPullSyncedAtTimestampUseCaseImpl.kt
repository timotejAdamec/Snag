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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.ports.PullSyncTimestampDb

internal class GetLastPullSyncedAtTimestampUseCaseImpl(
    private val timestampDb: PullSyncTimestampDb,
) : GetLastPullSyncedAtTimestampUseCase {
    override suspend fun invoke(
        entityType: String,
        scopeId: String,
    ): Timestamp? = timestampDb.getLastSyncedAt(entityType, scopeId)
}
