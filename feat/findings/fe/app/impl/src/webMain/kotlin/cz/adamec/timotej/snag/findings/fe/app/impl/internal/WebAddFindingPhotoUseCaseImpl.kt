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
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.WebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

internal class WebAddFindingPhotoUseCaseImpl(
    private val remoteFileStorage: RemoteFileStorage,
    private val findingPhotosDb: FindingPhotosDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : WebAddFindingPhotoUseCase {
    override suspend operator fun invoke(request: AddFindingPhotoRequest): OnlineDataResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension = request.fileName.substringAfterLast(delimiter = ".", missingDelimiterValue = "")
        val fileName = "$photoId.$extension"
        val directory = "projects/${request.projectId}/findings/${request.findingId}/photos"

        val uploadResult =
            remoteFileStorage.uploadFile(
                bytes = request.bytes,
                fileName = fileName,
                directory = directory,
            )

        return when (uploadResult) {
            is OnlineDataResult.Failure -> {
                logger.log(
                    offlineFirstDataResult = uploadResult,
                    additionalInfo = "WebAddFindingPhotoUseCase, remoteFileStorage.uploadFile failed",
                )
                uploadResult
            }

            is OnlineDataResult.Success -> {
                val remoteUrl = uploadResult.data
                val photo =
                    AppFindingPhotoData(
                        id = photoId,
                        findingId = request.findingId,
                        url = remoteUrl,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    )

                findingPhotosDb.savePhoto(photo)

                enqueueSyncSaveUseCase(
                    EnqueueSyncSaveRequest(
                        entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE,
                        entityId = photoId,
                    ),
                )

                OnlineDataResult.Success(photoId)
            }
        }
    }
}
