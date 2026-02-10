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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.sync

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.shared.database.fe.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealInspectionsPullSyncTimestampDataSource(
    private val queries: PullSyncTimestampEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : InspectionsPullSyncTimestampDataSource {
    override suspend fun getLastSyncedAt(projectId: Uuid): Timestamp? =
        withContext(ioDispatcher) {
            queries.getByEntityTypeAndScope(ENTITY_TYPE, projectId.toString()).awaitAsOneOrNull()?.let {
                Timestamp(it)
            }
        }

    override suspend fun setLastSyncedAt(
        projectId: Uuid,
        timestamp: Timestamp,
    ) {
        withContext(ioDispatcher) {
            queries.upsert(ENTITY_TYPE, projectId.toString(), timestamp.value)
        }
    }

    private companion object {
        private const val ENTITY_TYPE = "inspection"
    }
}
