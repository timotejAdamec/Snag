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

package cz.adamec.timotej.snag.projects.be.driven.impl.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

internal class InMemoryProjectsLocalDataSource(
    timestampProvider: TimestampProvider,
) : ProjectsLocalDataSource {
    private val projects =
        mutableListOf(
            BackendProject(
                project = Project(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Strahov Dormitories Renovation",
                    address = "Chaloupeckého 1917/9, 160 17 Praha 6",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendProject(
                project = Project(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                    name = "FIT CTU New Building",
                    address = "Thákurova 9, 160 00 Praha 6",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendProject(
                project = Project(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000003"),
                    name = "National Library of Technology",
                    address = "Technická 2710/6, 160 00 Praha 6",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
        )

    override suspend fun getProjects(): List<BackendProject> = projects

    override suspend fun getProject(id: Uuid): BackendProject? =
        projects
            .find { it.project.id == id }

    override suspend fun updateProject(project: BackendProject): BackendProject? {
        val foundProject = projects.find { it.project.id == project.project.id }
        if (foundProject != null && foundProject.project.updatedAt >= project.project.updatedAt) {
            return foundProject
        }

        projects.removeIf { it.project.id == project.project.id }
        projects.add(project)
        return null
    }

    override suspend fun deleteProject(id: Uuid, deletedAt: Timestamp): BackendProject? {
        val foundProject = projects.find { it.project.id == id }
        if (foundProject != null && foundProject.project.updatedAt >= deletedAt) {
            return foundProject
        }

        projects.removeIf { it.project.id == id && it.project.updatedAt < deletedAt }
        return null
    }
}
