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

import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project

internal class SaveProjectUseCaseImpl(
    private val projectsLocalDataSource: ProjectsLocalDataSource,
) : SaveProjectUseCase {
    override suspend operator fun invoke(project: Project): Project? {
        logger.debug("Saving project {} to local storage.", project)
        return projectsLocalDataSource.updateProject(project).also {
            logger.debug("Saved project {} to local storage.", project)
        }
    }
}
