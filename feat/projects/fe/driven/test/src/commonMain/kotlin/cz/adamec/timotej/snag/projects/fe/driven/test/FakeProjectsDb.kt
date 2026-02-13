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

import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityDb
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlin.uuid.Uuid

class FakeProjectsDb :
    FakeEntityDb<FrontendProject>(
        getId = { it.project.id },
    ),
    ProjectsDb {
    override fun getAllProjectsFlow() = allItemsFlow()

    override fun getProjectFlow(id: Uuid) = itemByIdFlow(id)

    override suspend fun saveProject(project: FrontendProject) = saveOneItem(project)

    override suspend fun saveProjects(projects: List<FrontendProject>) = saveManyItems(projects)

    override suspend fun deleteProject(id: Uuid) = deleteItem(id)

    fun setProject(project: FrontendProject) = setItem(project)
}
