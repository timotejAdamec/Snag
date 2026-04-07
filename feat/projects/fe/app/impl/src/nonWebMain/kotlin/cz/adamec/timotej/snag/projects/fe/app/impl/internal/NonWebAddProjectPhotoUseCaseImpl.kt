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
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoRequest
import cz.adamec.timotej.snag.projects.fe.app.api.NonWebAddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlinx.coroutines.CancellationException
import kotlin.uuid.Uuid

internal class NonWebAddProjectPhotoUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
    private val localFileStorage: LocalFileStorage,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : NonWebAddProjectPhotoUseCase {
    @Suppress("TooGenericExceptionCaught")
    override suspend operator fun invoke(request: AddProjectPhotoRequest): OfflineFirstDataResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension =
            request.fileName.substringAfterLast(
                delimiter = ".",
                missingDelimiterValue = "",
            )
        val localPath =
            try {
                localFileStorage.saveFile(
                    bytes = request.bytes,
                    fileName = "$photoId.$extension",
                    subdirectory = "projects/${request.projectId}/photos",
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(throwable = e) { "Failed to save project photo to local storage." }
                return OfflineFirstDataResult.ProgrammerError(throwable = e)
            }
        val photo =
            AppProjectPhotoData(
                id = photoId,
                projectId = request.projectId,
                url = localPath,
                description = request.description,
                updatedAt = timestampProvider.getNowTimestamp(),
            )

        return projectPhotosDb
            .savePhoto(photo)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "NonWebAddProjectPhotoUseCase, projectPhotosDb.savePhoto($photo)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        EnqueueSyncSaveRequest(
                            entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                            entityId = photoId,
                        ),
                    )
                }
            }.map {
                photoId
            }
    }
}
