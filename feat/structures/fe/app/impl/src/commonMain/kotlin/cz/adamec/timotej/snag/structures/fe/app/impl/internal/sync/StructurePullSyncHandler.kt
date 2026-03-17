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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeDeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class StructurePullSyncHandler(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
    private val cascadeDeleteLocalFindingsByStructureIdUseCase: CascadeDeleteLocalFindingsByStructureIdUseCase,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<StructureSyncResult>(
    logger = LH.logger,
    timestampProvider = timestampProvider,
    getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
) {
    override val entityTypeId: String = STRUCTURE_SYNC_ENTITY_TYPE
    override val entityName: String = "structure"

    override suspend fun fetchChangesFromApi(
        scopeId: String,
        since: Timestamp,
    ): OnlineDataResult<List<StructureSyncResult>> =
        structuresApi.getStructuresModifiedSince(
            projectId = Uuid.parse(scopeId),
            since = since,
        )

    override suspend fun applyChange(change: StructureSyncResult) {
        when (change) {
            is StructureSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted structure ${change.id}." }
                cascadeDeleteLocalFindingsByStructureIdUseCase(change.id)
                structuresDb.deleteStructure(change.id)
            }
            is StructureSyncResult.Updated -> {
                LH.logger.d { "Processing updated structure ${change.structure.structure.id}." }
                structuresDb.saveStructure(change.structure)
            }
        }
    }
}
