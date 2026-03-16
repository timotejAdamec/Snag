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

import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeRestoreLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class StructureSyncHandler(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
    private val cascadeRestoreLocalFindingsByStructureIdUseCase: CascadeRestoreLocalFindingsByStructureIdUseCase,
    timestampProvider: TimestampProvider,
) : DbApiSyncHandler<AppStructure>(LH.logger, timestampProvider) {
    override val entityTypeId: String = STRUCTURE_SYNC_ENTITY_TYPE
    override val entityName: String = "structure"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppStructure?>> = structuresDb.getStructureFlow(entityId)

    override suspend fun saveEntityToApi(entity: AppStructure): OnlineDataResult<AppStructure?> = structuresApi.saveStructure(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppStructure?> = structuresApi.deleteStructure(entityId, deletedAt)

    override suspend fun saveEntityToDb(entity: AppStructure): OfflineFirstDataResult<Unit> = structuresDb.saveStructure(entity)

    override suspend fun onDeleteRejected(entityId: Uuid) {
        cascadeRestoreLocalFindingsByStructureIdUseCase(entityId)
    }
}
