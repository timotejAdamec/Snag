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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource

internal class GetProjectsModifiedSinceUseCaseImpl(
    private val projectsLocalDataSource: ProjectsLocalDataSource,
) : GetProjectsModifiedSinceUseCase {
    override suspend operator fun invoke(since: Timestamp): List<BackendProject> {
        logger.debug("Getting projects modified since {} from local storage.", since)
        return projectsLocalDataSource.getProjectsModifiedSince(since).also {
            logger.debug("Got {} projects modified since {} from local storage.", it.size, since)
        }
    }
}
