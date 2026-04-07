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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncDeleteRequest
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class DeleteProjectPhotoUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : DeleteProjectPhotoUseCase {
    override suspend operator fun invoke(photoId: Uuid): OfflineFirstDataResult<Unit> {
        val photoResult = projectPhotosDb.getPhotoFlow(photoId).first()
        val projectId =
            when (photoResult) {
                is OfflineFirstDataResult.Success -> {
                    photoResult.data?.projectId
                }
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.log(offlineFirstDataResult = photoResult)
                    return photoResult
                }
            }

        projectPhotosDb.deletePhoto(photoId)

        if (projectId != null) {
            enqueueSyncDeleteUseCase(
                EnqueueSyncDeleteRequest(
                    entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                    entityId = photoId,
                    scopeId = projectId,
                ),
            )
        }

        return OfflineFirstDataResult.Success(Unit)
    }
}
