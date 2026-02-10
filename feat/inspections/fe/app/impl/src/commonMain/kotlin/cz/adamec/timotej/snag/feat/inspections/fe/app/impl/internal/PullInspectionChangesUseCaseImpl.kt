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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.PullInspectionChangesUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionSyncResult
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

internal class PullInspectionChangesUseCaseImpl(
    private val inspectionsApi: InspectionsApi,
    private val inspectionsDb: InspectionsDb,
    private val inspectionsPullSyncTimestampDataSource: InspectionsPullSyncTimestampDataSource,
    private val inspectionsPullSyncCoordinator: InspectionsPullSyncCoordinator,
    private val timestampProvider: TimestampProvider,
) : PullInspectionChangesUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        LH.logger.d { "Starting pull sync for inspections in project $projectId." }
        inspectionsPullSyncCoordinator.withFlushedQueue {
            val since = inspectionsPullSyncTimestampDataSource.getLastSyncedAt(projectId) ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()
            LH.logger.d { "Pulling inspection changes for project $projectId since=$since, now=$now." }

            when (val result = inspectionsApi.getInspectionsModifiedSince(projectId, since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w { "Error pulling inspection changes for project $projectId." }
                }
                is OnlineDataResult.Success -> {
                    val changes = result.data
                    LH.logger.d { "Received ${changes.size} inspection change(s) for project $projectId." }
                    changes.forEach { syncResult ->
                        when (syncResult) {
                            is InspectionSyncResult.Deleted -> {
                                LH.logger.d { "Processing deleted inspection ${syncResult.id}." }
                                inspectionsDb.deleteInspection(syncResult.id)
                            }
                            is InspectionSyncResult.Updated -> {
                                LH.logger.d { "Processing updated inspection ${syncResult.inspection.inspection.id}." }
                                inspectionsDb.saveInspection(syncResult.inspection)
                            }
                        }
                    }
                    inspectionsPullSyncTimestampDataSource.setLastSyncedAt(projectId, now)
                    LH.logger.d { "Pull sync for inspections in project $projectId completed, updated lastSyncedAt=$now." }
                }
            }
        }
    }
}
