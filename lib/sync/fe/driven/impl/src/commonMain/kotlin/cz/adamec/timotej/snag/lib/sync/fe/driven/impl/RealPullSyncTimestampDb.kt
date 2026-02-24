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

package cz.adamec.timotej.snag.lib.sync.fe.driven.impl

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.fe.ports.PullSyncTimestampDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class RealPullSyncTimestampDb(
    private val queries: PullSyncTimestampEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : PullSyncTimestampDb {
    override suspend fun getLastSyncedAt(
        entityType: String,
        scopeId: String,
    ): Timestamp? =
        withContext(ioDispatcher) {
            queries.getByEntityTypeAndScope(entityType, scopeId).awaitAsOneOrNull()?.let {
                Timestamp(it)
            }
        }

    override suspend fun setLastSyncedAt(
        entityType: String,
        scopeId: String,
        timestamp: Timestamp,
    ) {
        withContext(ioDispatcher) {
            queries.upsert(entityType, scopeId, timestamp.value)
        }
    }
}
