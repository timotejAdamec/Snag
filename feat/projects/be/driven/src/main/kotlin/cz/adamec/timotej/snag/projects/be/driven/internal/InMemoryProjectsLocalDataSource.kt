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

package cz.adamec.timotej.snag.projects.be.driven.internal

import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

internal class InMemoryProjectsLocalDataSource : ProjectsLocalDataSource {

    private val projects = listOf(
        Project(
            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
            name = "Strahov Dormitories Renovation",
            address = "Chaloupeckého 1917/9, 160 17 Praha 6",
        ),
        Project(
            id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
            name = "FIT CTU New Building",
            address = "Thákurova 9, 160 00 Praha 6",
        ),
        Project(
            id = Uuid.parse("00000000-0000-0000-0000-000000000003"),
            name = "National Library of Technology",
            address = "Technická 2710/6, 160 00 Praha 6",
        )
    )

    override suspend fun getProjects(): List<Project> = projects
}
