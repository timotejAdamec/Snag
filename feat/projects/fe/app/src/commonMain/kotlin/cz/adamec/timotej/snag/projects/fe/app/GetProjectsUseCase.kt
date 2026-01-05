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

import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsLocalDataSource
import kotlinx.coroutines.flow.Flow

class GetProjectsUseCase(
    private val localDataSource: ProjectsLocalDataSource,
) {
    operator fun invoke(): Flow<List<Project>> = localDataSource.getProjects()
}
