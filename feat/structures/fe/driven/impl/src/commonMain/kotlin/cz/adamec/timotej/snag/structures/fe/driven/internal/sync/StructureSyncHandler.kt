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

package cz.adamec.timotej.snag.structures.fe.driven.internal.sync

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class StructureSyncHandler(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
) : DbApiSyncHandler<FrontendStructure>(LH.logger) {
    override val entityTypeId: String = STRUCTURE_SYNC_ENTITY_TYPE
    override val entityName: String = "structure"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>> =
        structuresDb.getStructureFlow(entityId)

    override suspend fun saveEntityToApi(entity: FrontendStructure): OnlineDataResult<FrontendStructure?> =
        structuresApi.saveStructure(entity)

    override suspend fun deleteEntityFromApi(entityId: Uuid): OnlineDataResult<Unit> =
        structuresApi.deleteStructure(entityId)

    override suspend fun saveEntityToDb(entity: FrontendStructure): OfflineFirstDataResult<Unit> =
        structuresDb.saveStructure(entity)
}
