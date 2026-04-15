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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.PhotoUploadResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoStoragePort
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

internal class AddFindingPhotoUseCaseImpl(
    private val findingPhotoStoragePort: FindingPhotoStoragePort,
    private val findingPhotosDb: FindingPhotosDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : AddFindingPhotoUseCase {
    override suspend operator fun invoke(request: AddFindingPhotoRequest): PhotoUploadResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension = request.fileName.substringAfterLast(delimiter = ".", missingDelimiterValue = "")
        val fileName = "$photoId.$extension"
        val directory = "projects/${request.projectId}/findings/${request.findingId}/photos"

        return when (
            val uploadResult =
                findingPhotoStoragePort.uploadPhoto(
                    bytes = request.bytes,
                    fileName = fileName,
                    directory = directory,
                )
        ) {
            is PhotoUploadResult.Success -> {
                val photo =
                    AppFindingPhotoData(
                        id = photoId,
                        findingId = request.findingId,
                        url = uploadResult.data,
                        createdAt = timestampProvider.getNowTimestamp(),
                    )
                when (val dbResult = findingPhotosDb.savePhoto(photo)) {
                    is OfflineFirstDataResult.Success -> {
                        enqueueSyncSaveUseCase(
                            EnqueueSyncSaveRequest(
                                entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE,
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
