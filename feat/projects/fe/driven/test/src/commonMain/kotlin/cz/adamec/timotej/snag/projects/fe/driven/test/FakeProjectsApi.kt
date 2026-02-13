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

package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityApi
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

class FakeProjectsApi :
    FakeEntityApi<FrontendProject, ProjectSyncResult>(
        getId = { it.project.id },
    ),
    ProjectsApi {
    var saveProjectResponseOverride
        get() = saveResponseOverride
        set(value) {
            saveResponseOverride = value
        }

    override suspend fun getProjects() = getAllItems()

    override suspend fun getProject(id: Uuid) = getItemById(id)

    override suspend fun saveProject(project: FrontendProject) = saveItem(project)

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ) = deleteItemById(id)

    override suspend fun getProjectsModifiedSince(since: Timestamp) = getModifiedSinceItems()

    fun setProject(project: FrontendProject) = setItem(project)
}
