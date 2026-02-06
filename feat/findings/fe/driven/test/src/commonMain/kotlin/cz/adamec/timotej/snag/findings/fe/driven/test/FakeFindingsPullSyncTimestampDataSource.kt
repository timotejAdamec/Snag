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

package cz.adamec.timotej.snag.findings.fe.driven.test

import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

class FakeFindingsPullSyncTimestampDataSource : FindingsPullSyncTimestampDataSource {
    private val timestamps = mutableMapOf<Uuid, Timestamp>()

    override suspend fun getLastSyncedAt(structureId: Uuid): Timestamp? = timestamps[structureId]

    override suspend fun setLastSyncedAt(structureId: Uuid, timestamp: Timestamp) {
        timestamps[structureId] = timestamp
    }
}
