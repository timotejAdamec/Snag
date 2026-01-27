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

package cz.adamec.timotej.snag.projects.be.app

import cz.adamec.timotej.snag.projects.be.app.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import org.slf4j.Logger

class GetProjectsUseCase(
    private val projectsLocalDataSource: ProjectsLocalDataSource,
) {
    suspend operator fun invoke(): List<Project> {
        logger.debug("Getting projects from local storage.")
        return projectsLocalDataSource.getProjects().also {
            logger.debug("Got projects from local storage.")
        }
    }
}
