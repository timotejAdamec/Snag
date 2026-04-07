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

import cz.adamec.timotej.snag.projects.fe.app.api.CascadeDeleteLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import kotlin.uuid.Uuid

internal class CascadeDeleteLocalProjectPhotosByProjectIdUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
) : CascadeDeleteLocalProjectPhotosByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        projectPhotosDb.deletePhotosByProjectId(projectId)
    }
}
