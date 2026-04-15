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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoRequest
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoStoragePort
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

internal class AddProjectPhotoUseCaseImpl(
    private val projectPhotoStoragePort: ProjectPhotoStoragePort,
    private val projectPhotosDb: ProjectPhotosDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : AddProjectPhotoUseCase {
    override suspend operator fun invoke(
        request: AddProjectPhotoRequest,
        onProgress: (Float) -> Unit,
    ): PhotoUploadResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension =
            request.fileName.substringAfterLast(
                delimiter = ".",
                missingDelimiterValue = "",
            )
        val fileName = "$photoId.$extension"
        val directory = "projects/${request.projectId}/photos"

        return when (
            val uploadResult =
                projectPhotoStoragePort.uploadPhoto(
                    bytes = request.bytes,
                    fileName = fileName,
                    directory = directory,
                    onProgress = onProgress,
                )
        ) {
            is PhotoUploadResult.Success -> {
                val photo =
                    AppProjectPhotoData(
                        id = photoId,
                        projectId = request.projectId,
                        url = uploadResult.data,
                        description = request.description,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    )
                when (val dbResult = projectPhotosDb.savePhoto(photo)) {
                    is OfflineFirstDataResult.Success -> {
                        enqueueSyncSaveUseCase(
                            EnqueueSyncSaveRequest(
                                entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                                entityId = photoId,
                            ),
                        )
                        PhotoUploadResult.Success(photoId)
                    }

                    is OfflineFirstDataResult.ProgrammerError -> {
                        PhotoUploadResult.ProgrammerError(throwable = dbResult.throwable)
                    }
                }
            }

            is PhotoUploadResult.ProgrammerError -> {
                uploadResult
            }

            is PhotoUploadResult.NetworkUnavailable -> {
                uploadResult
            }

            is PhotoUploadResult.UserMessageError -> {
                uploadResult
            }
        }
    }
}
