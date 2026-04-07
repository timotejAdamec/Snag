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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPushSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class NonWebProjectPhotoSyncHandler(
    private val projectPhotosApi: ProjectPhotosApi,
    private val projectPhotosDb: ProjectPhotosDb,
    private val localFileStorage: LocalFileStorage,
    private val remoteFileStorage: RemoteFileStorage,
    timestampProvider: TimestampProvider,
) : DbApiPushSyncHandler<AppProjectPhoto>(LH.logger, timestampProvider) {
    override val entityTypeId: String = PROJECT_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "project_photo"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppProjectPhoto?>> = projectPhotosDb.getPhotoFlow(entityId)

    override suspend fun saveEntityToDb(entity: AppProjectPhoto): OfflineFirstDataResult<Unit> = projectPhotosDb.savePhoto(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
        scopeId: Uuid?,
    ): OnlineDataResult<AppProjectPhoto?> =
        projectPhotosApi.deletePhoto(
            id = entityId,
            projectId = scopeId!!,
            deletedAt = deletedAt,
        )

    @Suppress("ReturnCount")
    override suspend fun saveEntityToApi(entity: AppProjectPhoto): OnlineDataResult<AppProjectPhoto?> {
        if (entity.url.startsWith("http")) {
            return projectPhotosApi.savePhoto(entity)
        }

        val bytes = localFileStorage.readFileBytes(entity.url)
        val fileName = entity.url.substringAfterLast("/")

        val uploadResult =
            remoteFileStorage.uploadFile(
                bytes = bytes,
                fileName = fileName,
                directory = "projects/${entity.projectId}/photos",
            )

        return when (uploadResult) {
            is OnlineDataResult.Failure -> {
                uploadResult
            }
            is OnlineDataResult.Success -> {
                localFileStorage.deleteFile(entity.url)
                val updatedPhoto =
                    AppProjectPhotoData(
                        id = entity.id,
                        projectId = entity.projectId,
                        url = uploadResult.data,
                        description = entity.description,
                        updatedAt = entity.updatedAt,
                    )
                projectPhotosDb.savePhoto(updatedPhoto)
                projectPhotosApi.savePhoto(updatedPhoto)
            }
        }
    }
}
