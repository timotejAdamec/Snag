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
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb

internal class DeleteProjectUseCaseImpl(
    private val projectsDb: ProjectsDb,
) : DeleteProjectUseCase {
    override suspend operator fun invoke(request: DeleteProjectRequest): BackendProject? {
        logger.debug("Deleting project {} from local storage.", request.projectId)
        return projectsDb
            .deleteProject(
                id = request.projectId,
                deletedAt = request.deletedAt,
            ).also { newerProject ->
                newerProject?.let {
                    logger.debug(
                        "Found newer version of project {} in local storage. Returning it instead.",
                        request.projectId,
                    )
                } ?: logger.debug("Deleted project {} from local storage.", request.projectId)
            }
    }
}
