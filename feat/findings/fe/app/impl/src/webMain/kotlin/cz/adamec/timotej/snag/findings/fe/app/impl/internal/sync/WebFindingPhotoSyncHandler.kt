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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPushSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class WebFindingPhotoSyncHandler(
    private val findingPhotosApi: FindingPhotosApi,
    private val findingPhotosDb: FindingPhotosDb,
    timestampProvider: TimestampProvider,
) : DbApiPushSyncHandler<AppFindingPhoto>(LH.logger, timestampProvider) {
    override val entityTypeId: String = FINDING_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "finding_photo"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppFindingPhoto?>> =
        findingPhotosDb.getPhotoFlow(entityId)

    override suspend fun saveEntityToDb(entity: AppFindingPhoto): OfflineFirstDataResult<Unit> =
        findingPhotosDb.savePhoto(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
        scopeId: Uuid?,
    ): OnlineDataResult<AppFindingPhoto?> =
        findingPhotosApi.deletePhoto(
            id = entityId,
            findingId = scopeId!!,
            deletedAt = deletedAt,
        )

    override suspend fun saveEntityToApi(entity: AppFindingPhoto): OnlineDataResult<AppFindingPhoto?> =
        findingPhotosApi.savePhoto(entity)
}
