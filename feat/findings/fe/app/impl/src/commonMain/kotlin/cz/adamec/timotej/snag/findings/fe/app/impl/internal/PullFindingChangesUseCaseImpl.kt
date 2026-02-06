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
        LH.logger.d { "Starting pull sync for findings in structure $structureId." }
        findingsPullSyncCoordinator.withFlushedQueue {
            val since = findingsPullSyncTimestampDataSource.getLastSyncedAt(structureId) ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()
            LH.logger.d { "Pulling finding changes for structure $structureId since=$since, now=$now." }

            when (val result = findingsApi.getFindingsModifiedSince(structureId, since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w { "Error pulling finding changes for structure $structureId." }
                }
                is OnlineDataResult.Success -> {
                    val changes = result.data
                    LH.logger.d { "Received ${changes.size} finding change(s) for structure $structureId." }
                    changes.forEach { syncResult ->
                        when (syncResult) {
                            is FindingSyncResult.Deleted -> {
                                LH.logger.d { "Processing deleted finding ${syncResult.id}." }
                                findingsDb.deleteFinding(syncResult.id)
                            }
                            is FindingSyncResult.Updated -> {
                                LH.logger.d { "Processing updated finding ${syncResult.finding.finding.id}." }
                                findingsDb.saveFinding(syncResult.finding)
                            }
                        }
                    }
                    findingsPullSyncTimestampDataSource.setLastSyncedAt(structureId, now)
                    LH.logger.d { "Pull sync for findings in structure $structureId completed, updated lastSyncedAt=$now." }
                }
            }
        }
    }
}
