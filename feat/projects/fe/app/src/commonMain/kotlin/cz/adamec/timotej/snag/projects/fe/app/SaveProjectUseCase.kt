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

package cz.adamec.timotej.snag.projects.fe.app

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.lib.core.UuidProvider
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.model.SaveProjectRequest
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository

class SaveProjectUseCase(
    private val projectsRepository: ProjectsRepository,
    private val uuidProvider: UuidProvider,
) {
    suspend operator fun invoke(request: SaveProjectRequest): DataResult<Unit> = projectsRepository.saveProject(
        Project(
            id = uuidProvider.getUuid(),
            name = request.name,
            address = request.address
        )
    )
}
