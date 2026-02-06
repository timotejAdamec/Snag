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

package cz.adamec.timotej.snag.projects.fe.driven.internal.sync

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncTimestampDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class RealProjectsPullSyncTimestampDataSource(
    private val queries: PullSyncTimestampEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ProjectsPullSyncTimestampDataSource {
    override suspend fun getLastSyncedAt(): Timestamp? =
        withContext(ioDispatcher) {
            queries.getByEntityTypeAndScope(ENTITY_TYPE, null).awaitAsOneOrNull()?.let {
                Timestamp(it)
            }
        }

    override suspend fun setLastSyncedAt(timestamp: Timestamp) {
        withContext(ioDispatcher) {
            queries.upsert(ENTITY_TYPE, null, timestamp.value)
        }
    }

    private companion object {
        private const val ENTITY_TYPE = "project"
    }
}
