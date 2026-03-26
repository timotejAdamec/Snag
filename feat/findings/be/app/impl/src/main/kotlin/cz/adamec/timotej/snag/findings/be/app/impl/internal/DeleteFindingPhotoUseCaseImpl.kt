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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingPhotoRequest
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb

internal class DeleteFindingPhotoUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
) : DeleteFindingPhotoUseCase {
    override suspend operator fun invoke(request: DeleteFindingPhotoRequest): BackendFindingPhoto? {
        logger.debug("Deleting finding photo {} from local storage.", request.photoId)
        val isRejected =
            findingPhotosDb.deletePhoto(id = request.photoId, deletedAt = request.deletedAt)
        if (isRejected != null) {
            logger.debug(
                "Found newer version of finding photo {} in local storage. Returning it instead.",
                request.photoId,
            )
        } else {
            logger.debug("Deleted finding photo {} from local storage.", request.photoId)
        }
        return isRejected
    }
}
