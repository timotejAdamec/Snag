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

import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

class GetProjectUseCase(
    private val projectsLocalDataSource: ProjectsLocalDataSource,
) {
    suspend operator fun invoke(id: Uuid): Project? = projectsLocalDataSource.getProject(id)
}
