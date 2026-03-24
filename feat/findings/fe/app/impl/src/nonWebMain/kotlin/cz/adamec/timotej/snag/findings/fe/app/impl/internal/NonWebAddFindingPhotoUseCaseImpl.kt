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
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.NonWebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

internal class NonWebAddFindingPhotoUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
    private val localFileStorage: LocalFileStorage,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : NonWebAddFindingPhotoUseCase {
    @Suppress("TooGenericExceptionCaught")
    override suspend operator fun invoke(request: AddFindingPhotoRequest): OfflineFirstDataResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension = request.fileName.substringAfterLast(delimiter = ".", missingDelimiterValue = "")
        val localPath =
            try {
                localFileStorage.saveFile(
                    bytes = request.bytes,
                    fileName = "$photoId.$extension",
                    subdirectory = "projects/${request.projectId}/findings/${request.findingId}/photos",
                )
            } catch (e: Exception) {
                logger.e(throwable = e) { "Failed to save photo to local storage." }
                return OfflineFirstDataResult.ProgrammerError(throwable = e)
            }
        val photo =
            AppFindingPhotoData(
                id = photoId,
                findingId = request.findingId,
                url = localPath,
                createdAt = timestampProvider.getNowTimestamp(),
            )

        return findingPhotosDb
            .savePhoto(photo)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "NonWebAddFindingPhotoUseCase, findingPhotosDb.savePhoto($photo)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        EnqueueSyncSaveRequest(
                            entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE,
                            entityId = photoId,
                        ),
                    )
                }
            }.map {
                photoId
            }
    }
}
