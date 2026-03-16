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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class InspectionSyncHandler(
    private val inspectionsApi: InspectionsApi,
    private val inspectionsDb: InspectionsDb,
    timestampProvider: TimestampProvider,
) : DbApiSyncHandler<AppInspection>(LH.logger, timestampProvider) {
    override val entityTypeId: String = INSPECTION_SYNC_ENTITY_TYPE
    override val entityName: String = "inspection"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppInspection?>> =
        inspectionsDb.getInspectionFlow(entityId)

    override suspend fun saveEntityToApi(entity: AppInspection): OnlineDataResult<AppInspection?> =
        inspectionsApi.saveInspection(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppInspection?> = inspectionsApi.deleteInspection(entityId, deletedAt)

    override suspend fun saveEntityToDb(entity: AppInspection): OfflineFirstDataResult<Unit> = inspectionsDb.saveInspection(entity)
}
