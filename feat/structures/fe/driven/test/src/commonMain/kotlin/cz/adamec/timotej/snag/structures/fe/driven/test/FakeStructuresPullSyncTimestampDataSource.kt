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

package cz.adamec.timotej.snag.structures.fe.driven.test

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import kotlin.uuid.Uuid

class FakeStructuresPullSyncTimestampDataSource : StructuresPullSyncTimestampDataSource {
    private val timestamps = mutableMapOf<Uuid, Timestamp>()

    override suspend fun getLastSyncedAt(projectId: Uuid): Timestamp? = timestamps[projectId]

    override suspend fun setLastSyncedAt(projectId: Uuid, timestamp: Timestamp) {
        timestamps[projectId] = timestamp
    }
}
