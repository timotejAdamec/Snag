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

package cz.adamec.timotej.snag.clients.fe.driven.internal.sync

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class RealClientsPullSyncTimestampDataSource(
    private val queries: PullSyncTimestampEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ClientsPullSyncTimestampDataSource {
    override suspend fun getLastSyncedAt(): Timestamp? =
        withContext(ioDispatcher) {
            queries.getByEntityTypeAndScope(ENTITY_TYPE, GLOBAL_SCOPE_ID).awaitAsOneOrNull()?.let {
                Timestamp(it)
            }
        }

    override suspend fun setLastSyncedAt(timestamp: Timestamp) {
        withContext(ioDispatcher) {
            queries.upsert(ENTITY_TYPE, GLOBAL_SCOPE_ID, timestamp.value)
        }
    }

    private companion object {
        private const val ENTITY_TYPE = "client"
        private const val GLOBAL_SCOPE_ID = ""
    }
}
