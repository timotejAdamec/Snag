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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImpl(
    private val projectPhotosApi: ProjectPhotosApi,
    private val projectPhotosDb: ProjectPhotosDb,
) : CascadeRestoreLocalProjectPhotosByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        when (val result = projectPhotosApi.getPhotosModifiedSince(projectId = projectId, since = Timestamp(0L))) {
            is OnlineDataResult.Success -> {
                projectPhotosDb.deletePhotosByProjectId(projectId)
                result.data.forEach { syncResult ->
                    when (syncResult) {
                        is ProjectPhotoSyncResult.Updated -> {
                            projectPhotosDb.savePhoto(syncResult.photo)
                        }
                        is ProjectPhotoSyncResult.Deleted -> {
                            // Already cleared above
                        }
                    }
                }
            }
            is OnlineDataResult.Failure -> {
                logger.w { "Failed to restore project photos for project $projectId: $result" }
            }
        }
    }
}
