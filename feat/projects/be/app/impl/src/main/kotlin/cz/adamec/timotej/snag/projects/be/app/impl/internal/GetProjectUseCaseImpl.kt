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

import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import kotlin.uuid.Uuid

internal class GetProjectUseCaseImpl(
    private val projectsDb: ProjectsDb,
) : GetProjectUseCase {
    override suspend operator fun invoke(id: Uuid): BackendProject? {
        logger.debug("Getting project {} from local storage.", id)
        return projectsDb.getProject(id).also {
            logger.debug("Got project {} from local storage.", id)
        }
    }
}
