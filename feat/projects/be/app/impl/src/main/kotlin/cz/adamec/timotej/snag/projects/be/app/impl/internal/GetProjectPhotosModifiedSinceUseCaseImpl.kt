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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.GetProjectPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.GetProjectPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb

internal class GetProjectPhotosModifiedSinceUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
) : GetProjectPhotosModifiedSinceUseCase {
    override suspend operator fun invoke(request: GetProjectPhotosModifiedSinceRequest): List<BackendProjectPhoto> =
        projectPhotosDb.getPhotosModifiedSince(
            projectId = request.projectId,
            since = request.since,
        )
}
