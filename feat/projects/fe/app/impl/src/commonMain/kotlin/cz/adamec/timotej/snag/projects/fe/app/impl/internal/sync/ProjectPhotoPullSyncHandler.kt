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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class ProjectPhotoPullSyncHandler(
    private val projectPhotosApi: ProjectPhotosApi,
    private val projectPhotosDb: ProjectPhotosDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<ProjectPhotoSyncResult>(
        logger = LH.logger,
        timestampProvider = timestampProvider,
        getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
        setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
    ) {
    override val entityTypeId: String = PROJECT_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "project_photo"

    override suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<ProjectPhotoSyncResult>> =
        projectPhotosApi.getPhotosModifiedSince(
            projectId = scopeId!!,
            since = since,
        )

    override suspend fun applyChange(change: ProjectPhotoSyncResult) {
        when (change) {
            is ProjectPhotoSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted project photo ${change.id}." }
                projectPhotosDb.deletePhoto(change.id)
            }
            is ProjectPhotoSyncResult.Updated -> {
                LH.logger.d { "Processing updated project photo ${change.photo.id}." }
                projectPhotosDb.savePhoto(change.photo)
            }
        }
    }
}
