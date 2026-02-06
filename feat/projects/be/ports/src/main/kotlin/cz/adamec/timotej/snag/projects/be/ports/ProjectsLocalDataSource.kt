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

package cz.adamec.timotej.snag.projects.be.ports

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import kotlin.uuid.Uuid

interface ProjectsLocalDataSource {
    suspend fun getProjects(): List<BackendProject>

    suspend fun getProject(id: Uuid): BackendProject?

    suspend fun updateProject(project: BackendProject): BackendProject?

    suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProject?

    suspend fun getProjectsModifiedSince(since: Timestamp): List<BackendProject>
}
