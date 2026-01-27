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
    private val projects =
        mutableListOf(
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
            ),
        )

//    val now = Instant.parse("2023-10-27T10:00:00Z")
//    projects.addAll(
//    listOf(
//    Project(
//    id = UuidProvider.getUuid(),
//    name = "Central Park Renovation",
//    address = "New York, NY 10024",
// //                    client = ClientImpl(1, "City Parks Dept", "830 5th Ave"),
// //                    inspector = InspectorImpl(1, "John Doe", "+12125550199"),
// //                    inspection = InspectionImpl(
// //                        start = now,
// //                        end = now + 2.hours,
// //                        otherParticipants = persistentListOf("Alice Freeman", "Bob Smith"),
// //                        climateConditions = "Sunny, 20°C",
// //                        note = "Initial site survey completed."
// //                    ),
// //                    findingCategories = persistentListOf("Safety", "Landscaping")
//    ),
//    Project(
//    id = UuidProvider.getUuid(),
//    name = "Golden Gate Bridge Painting",
//    address = "San Francisco, CA 94129",
// //                    client = ClientImpl(2, "Caltrans", "1120 N St, Sacramento"),
// //                    inspector = InspectorImpl(2, "Jane Smith", "+14155550122"),
// //                    inspection = null,
// //                    findingCategories = persistentListOf("Structure", "Paint Quality")
//    ),
//    Project(
//    id = UuidProvider.getUuid(),
//    name = "Space Needle Maintenance",
//    address = "400 Broad St, Seattle, WA 98109",
// //                    client = ClientImpl(3, "Space Needle Corp", null),
// //                    inspector = InspectorImpl(3, "Bob Wilson", "+12065550144"),
// //                    inspection = null,
// //                    findingCategories = persistentListOf("Elevators", "Structural")
//    ),
//    ),
//    )

    override suspend fun getProjects(): List<Project> = projects

    override suspend fun getProject(id: Uuid): Project? =
        projects
            .find { it.id == id }

    // TODO check updated timestamp and return the database project if it is newer
    override suspend fun updateProject(project: Project): Project? {
        projects.removeIf { it.id == project.id }
        projects.add(project)
        return null
    }

    override suspend fun deleteProject(id: Uuid) {
        projects.removeIf { it.id == id }
    }
}
