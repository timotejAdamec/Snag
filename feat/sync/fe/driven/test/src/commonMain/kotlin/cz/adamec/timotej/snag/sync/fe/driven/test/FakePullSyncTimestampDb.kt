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

package cz.adamec.timotej.snag.sync.fe.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.fe.ports.PullSyncTimestampDb

class FakePullSyncTimestampDb : PullSyncTimestampDb {
    private val timestamps = mutableMapOf<Pair<String, String>, Timestamp>()

    override suspend fun getLastSyncedAt(
        entityType: String,
        scopeId: String,
    ): Timestamp? = timestamps[entityType to scopeId]

    override suspend fun setLastSyncedAt(
        entityType: String,
        scopeId: String,
        timestamp: Timestamp,
    ) {
        timestamps[entityType to scopeId] = timestamp
    }
}
