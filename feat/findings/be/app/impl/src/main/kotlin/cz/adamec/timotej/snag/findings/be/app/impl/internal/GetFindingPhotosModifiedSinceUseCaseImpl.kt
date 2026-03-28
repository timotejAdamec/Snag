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
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb

internal class GetFindingPhotosModifiedSinceUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
) : GetFindingPhotosModifiedSinceUseCase {
    override suspend operator fun invoke(request: GetFindingPhotosModifiedSinceRequest): List<BackendFindingPhoto> {
        logger.debug(
            "Getting finding photos modified since {} for finding {} from local storage.",
            request.since,
            request.findingId,
        )
        val photos =
            findingPhotosDb.getPhotosModifiedSince(
                findingId = request.findingId,
                since = request.since,
            )
        logger.debug(
            "Got {} finding photos modified since {} for finding {} from local storage.",
            photos.size,
            request.since,
            request.findingId,
        )
        return photos
    }
}
