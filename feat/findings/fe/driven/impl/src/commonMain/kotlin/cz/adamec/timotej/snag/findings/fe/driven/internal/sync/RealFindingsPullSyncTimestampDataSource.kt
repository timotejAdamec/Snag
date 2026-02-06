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

package cz.adamec.timotej.snag.findings.fe.driven.internal.sync

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealFindingsPullSyncTimestampDataSource(
    private val queries: PullSyncTimestampEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : FindingsPullSyncTimestampDataSource {
    override suspend fun getLastSyncedAt(structureId: Uuid): Timestamp? =
        withContext(ioDispatcher) {
            queries.getByEntityTypeAndScope(ENTITY_TYPE, structureId.toString()).awaitAsOneOrNull()?.let {
                Timestamp(it)
            }
        }

    override suspend fun setLastSyncedAt(structureId: Uuid, timestamp: Timestamp) {
        withContext(ioDispatcher) {
            queries.upsert(ENTITY_TYPE, structureId.toString(), timestamp.value)
        }
    }

    private companion object {
        private const val ENTITY_TYPE = "finding"
    }
}
