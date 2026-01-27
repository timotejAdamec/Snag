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

package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

interface ProjectsApi {
    suspend fun getProjects(): OnlineDataResult<List<Project>>

    suspend fun getProject(id: Uuid): OnlineDataResult<Project>

    suspend fun saveProject(project: Project): OnlineDataResult<Project?>

    suspend fun deleteProject(id: Uuid): OnlineDataResult<Unit>
}
