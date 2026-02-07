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
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

internal class InMemoryProjectsDb(
    timestampProvider: TimestampProvider,
) : ProjectsDb {
    private val projects =
        mutableListOf(
            BackendProject(
                project =
                    Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Strahov Dormitories Renovation",
                        address = "Chaloupeckého 1917/9, 160 17 Praha 6",
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            ),
            BackendProject(
                project =
                    Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                        name = "FIT CTU New Building",
                        address = "Thákurova 9, 160 00 Praha 6",
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            ),
            BackendProject(
                project =
                    Project(
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
        if (foundProject != null) {
            val serverTimestamp =
                maxOf(
                    foundProject.project.updatedAt,
                    foundProject.deletedAt ?: Timestamp(0),
                )
            if (serverTimestamp >= project.project.updatedAt) {
                return foundProject
            }
        }

        projects.removeIf { it.project.id == project.project.id }
        projects.add(project)
        return null
    }

    @Suppress("ReturnCount")
    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProject? {
        val foundProject =
            projects.find { it.project.id == id }
                ?: return null
        if (foundProject.deletedAt != null) return null
        if (foundProject.project.updatedAt >= deletedAt) return foundProject

        val index = projects.indexOfFirst { it.project.id == id }
        projects[index] = foundProject.copy(deletedAt = deletedAt)
        return null
    }

    override suspend fun getProjectsModifiedSince(since: Timestamp): List<BackendProject> =
        projects.filter {
            it.project.updatedAt > since || it.deletedAt?.let { d -> d > since } == true
        }
}
