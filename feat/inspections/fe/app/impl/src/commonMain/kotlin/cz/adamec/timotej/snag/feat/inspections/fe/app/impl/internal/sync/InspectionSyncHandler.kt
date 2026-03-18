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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPushSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class InspectionSyncHandler(
    private val inspectionsApi: InspectionsApi,
    private val inspectionsDb: InspectionsDb,
    timestampProvider: TimestampProvider,
) : DbApiPushSyncHandler<AppInspection>(LH.logger, timestampProvider) {
    override val entityTypeId: String = INSPECTION_SYNC_ENTITY_TYPE
    override val entityName: String = "inspection"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppInspection?>> = inspectionsDb.getInspectionFlow(entityId)

    override suspend fun saveEntityToApi(entity: AppInspection): OnlineDataResult<AppInspection?> = inspectionsApi.saveInspection(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppInspection?> = inspectionsApi.deleteInspection(entityId, deletedAt)

    override suspend fun saveEntityToDb(entity: AppInspection): OfflineFirstDataResult<Unit> = inspectionsDb.saveInspection(entity)
}
