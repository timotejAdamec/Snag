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

import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import kotlin.uuid.Uuid

internal class DeleteProjectUseCaseImpl(
    private val projectsLocalDataSource: ProjectsLocalDataSource,
) : DeleteProjectUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        logger.debug("Deleting project {} from local storage.", projectId)
        projectsLocalDataSource.deleteProject(projectId)
        logger.debug("Deleted project {} from local storage.", projectId)
    }
}
