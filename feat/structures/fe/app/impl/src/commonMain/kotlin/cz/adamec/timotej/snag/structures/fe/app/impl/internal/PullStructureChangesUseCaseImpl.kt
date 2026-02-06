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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.findings.fe.app.api.DeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.PullStructureChangesUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import kotlin.uuid.Uuid

internal class PullStructureChangesUseCaseImpl(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
    private val deleteLocalFindingsByStructureIdUseCase: DeleteLocalFindingsByStructureIdUseCase,
    private val structuresPullSyncTimestampDataSource: StructuresPullSyncTimestampDataSource,
    private val structuresPullSyncCoordinator: StructuresPullSyncCoordinator,
    private val timestampProvider: TimestampProvider,
) : PullStructureChangesUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        LH.logger.d { "Starting pull sync for structures in project $projectId." }
        structuresPullSyncCoordinator.withFlushedQueue {
            val since = structuresPullSyncTimestampDataSource.getLastSyncedAt(projectId) ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()
            LH.logger.d { "Pulling structure changes for project $projectId since=$since, now=$now." }

            when (val result = structuresApi.getStructuresModifiedSince(projectId, since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w { "Error pulling structure changes for project $projectId." }
                }
                is OnlineDataResult.Success -> {
                    val changes = result.data
                    LH.logger.d { "Received ${changes.size} structure change(s) for project $projectId." }
                    changes.forEach { syncResult ->
                        when (syncResult) {
                            is StructureSyncResult.Deleted -> {
                                LH.logger.d { "Processing deleted structure ${syncResult.id}." }
                                deleteLocalFindingsByStructureIdUseCase(syncResult.id)
                                structuresDb.deleteStructure(syncResult.id)
                            }
                            is StructureSyncResult.Updated -> {
                                LH.logger.d { "Processing updated structure ${syncResult.structure.structure.id}." }
                                structuresDb.saveStructure(syncResult.structure)
                            }
                        }
                    }
                    structuresPullSyncTimestampDataSource.setLastSyncedAt(projectId, now)
                    LH.logger.d { "Pull sync for structures in project $projectId completed, updated lastSyncedAt=$now." }
                }
            }
        }
    }
}
