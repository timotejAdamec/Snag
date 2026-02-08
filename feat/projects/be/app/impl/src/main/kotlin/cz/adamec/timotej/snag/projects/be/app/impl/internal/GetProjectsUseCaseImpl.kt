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

import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb

internal class GetProjectsUseCaseImpl(
    private val projectsDb: ProjectsDb,
) : GetProjectsUseCase {
    override suspend operator fun invoke(): List<BackendProject> {
        logger.debug("Getting projects from local storage.")
        return projectsDb.getProjects().also {
            logger.debug("Got projects from local storage.")
        }
    }
}
