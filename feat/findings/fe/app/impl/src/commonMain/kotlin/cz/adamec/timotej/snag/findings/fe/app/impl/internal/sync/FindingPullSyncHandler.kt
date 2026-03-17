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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class FindingPullSyncHandler(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<FindingSyncResult>(
    logger = LH.logger,
    timestampProvider = timestampProvider,
    getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
) {
    override val entityTypeId: String = FINDING_SYNC_ENTITY_TYPE
    override val entityName: String = "finding"

    override suspend fun fetchChangesFromApi(
        scopeId: String,
        since: Timestamp,
    ): OnlineDataResult<List<FindingSyncResult>> =
        findingsApi.getFindingsModifiedSince(
            structureId = Uuid.parse(scopeId),
            since = since,
        )

    override suspend fun applyChange(change: FindingSyncResult) {
        when (change) {
            is FindingSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted finding ${change.id}." }
                findingsDb.deleteFinding(change.id)
            }
            is FindingSyncResult.Updated -> {
                LH.logger.d { "Processing updated finding ${change.finding.finding.id}." }
                findingsDb.saveFinding(change.finding)
            }
        }
    }
}
