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
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class FindingPhotoPullSyncHandler(
    private val findingPhotosApi: FindingPhotosApi,
    private val findingPhotosDb: FindingPhotosDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<FindingPhotoSyncResult>(
        logger = LH.logger,
        timestampProvider = timestampProvider,
        getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
        setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
    ) {
    override val entityTypeId: String = FINDING_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "finding_photo"

    override suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<FindingPhotoSyncResult>> =
        findingPhotosApi.getPhotosModifiedSince(
            findingId = scopeId!!,
            since = since,
        )

    override suspend fun applyChange(change: FindingPhotoSyncResult) {
        when (change) {
            is FindingPhotoSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted finding photo ${change.id}." }
                findingPhotosDb.deletePhoto(change.id)
            }
            is FindingPhotoSyncResult.Updated -> {
                LH.logger.d { "Processing updated finding photo ${change.photo.id}." }
                findingPhotosDb.savePhoto(change.photo)
            }
        }
    }
}
