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

import cz.adamec.timotej.snag.lib.core.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlin.uuid.Uuid

class DeleteProjectUseCase(
    private val projectsRepository: ProjectsRepository,
) {
    suspend operator fun invoke(projectId: Uuid): OfflineFirstDataResult<Unit> =
        projectsRepository.deleteProject(projectId)
}
