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

package cz.adamec.timotej.snag.clients.fe.driven.test

import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp

class FakeClientsPullSyncTimestampDataSource : ClientsPullSyncTimestampDataSource {
    private var lastSyncedAt: Timestamp? = null

    override suspend fun getLastSyncedAt(): Timestamp? = lastSyncedAt

    override suspend fun setLastSyncedAt(timestamp: Timestamp) {
        lastSyncedAt = timestamp
    }
}
