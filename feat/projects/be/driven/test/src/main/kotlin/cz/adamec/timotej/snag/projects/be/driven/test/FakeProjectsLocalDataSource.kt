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

package cz.adamec.timotej.snag.projects.be.driven.test

import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import kotlin.uuid.Uuid

class FakeProjectsLocalDataSource : ProjectsLocalDataSource {
    private val projects = mutableMapOf<Uuid, BackendProject>()

    override suspend fun getProjects(): List<BackendProject> = projects.values.toList()

    override suspend fun getProject(id: Uuid): BackendProject? = projects[id]

    override suspend fun updateProject(project: BackendProject): BackendProject? {
        projects[project.project.id] = project
        return null
    }

    override suspend fun deleteProject(id: Uuid) {
        projects.remove(id)
    }

    fun setProject(project: BackendProject) {
        projects[project.project.id] = project
    }
}
