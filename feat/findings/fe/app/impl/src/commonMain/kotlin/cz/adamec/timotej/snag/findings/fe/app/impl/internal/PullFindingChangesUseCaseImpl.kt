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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.findings.fe.app.api.PullFindingChangesUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncCoordinator
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

internal class PullFindingChangesUseCaseImpl(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
    private val findingsPullSyncTimestampDataSource: FindingsPullSyncTimestampDataSource,
    private val findingsPullSyncCoordinator: FindingsPullSyncCoordinator,
    private val timestampProvider: TimestampProvider,
) : PullFindingChangesUseCase {
    override suspend operator fun invoke(structureId: Uuid) {
        findingsPullSyncCoordinator.withFlushedQueue {
            val since = findingsPullSyncTimestampDataSource.getLastSyncedAt(structureId) ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()

            when (val result = findingsApi.getFindingsModifiedSince(structureId, since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w("Error pulling finding changes for structure $structureId.")
                }
                is OnlineDataResult.Success -> {
                    result.data.forEach { syncResult ->
                        when (syncResult) {
                            is FindingSyncResult.Deleted -> {
                                findingsDb.deleteFinding(syncResult.id)
                            }
                            is FindingSyncResult.Updated -> {
                                findingsDb.saveFinding(syncResult.finding)
                            }
                        }
                    }
                    findingsPullSyncTimestampDataSource.setLastSyncedAt(structureId, now)
                }
            }
        }
    }
}
