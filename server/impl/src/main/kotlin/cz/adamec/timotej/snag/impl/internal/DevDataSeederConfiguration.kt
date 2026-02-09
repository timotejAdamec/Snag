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

package cz.adamec.timotej.snag.impl.internal

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking
import kotlin.uuid.Uuid

internal class DevDataSeederConfiguration(
    private val projectsDb: ProjectsDb,
    private val structuresDb: StructuresDb,
    private val findingsDb: FindingsDb,
    private val timestampProvider: TimestampProvider,
) : AppConfiguration {
    override fun Application.setup() {
        runBlocking {
            seedProjects()
            seedStructures()
            seedFindings()
        }
    }

    private suspend fun seedProjects() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendProject(
                project =
                    Project(
                        id = Uuid.parse(PROJECT_1),
                        name = "Strahov Dormitories Renovation",
                        address = "Chaloupeckého 1917/9, 160 17 Praha 6",
                        updatedAt = now,
                    ),
            ),
            BackendProject(
                project =
                    Project(
                        id = Uuid.parse(PROJECT_2),
                        name = "FIT CTU New Building",
                        address = "Thákurova 9, 160 00 Praha 6",
                        updatedAt = now,
                    ),
            ),
            BackendProject(
                project =
                    Project(
                        id = Uuid.parse(PROJECT_3),
                        name = "National Library of Technology",
                        address = "Technická 2710/6, 160 00 Praha 6",
                        updatedAt = now,
                    ),
            ),
        ).forEach { projectsDb.saveProject(it) }
    }

    private suspend fun seedStructures() {
        val now = timestampProvider.getNowTimestamp()
        (project1Structures(now) + project2Structures(now) + project3Structures(now))
            .forEach { structuresDb.saveStructure(it) }
    }

    private fun project1Structures(now: Timestamp) =
        listOf(
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_1),
                        projectId = Uuid.parse(PROJECT_1),
                        name = "Block A - Ground Floor",
                        floorPlanUrl = "https://upload.wikimedia.org/wikipedia/commons/9/9a/Sample_Floorplan.jpg",
                        updatedAt = now,
                    ),
            ),
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_2),
                        projectId = Uuid.parse(PROJECT_1),
                        name = "Block A - First Floor",
                        floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
                        updatedAt = now,
                    ),
            ),
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_3),
                        projectId = Uuid.parse(PROJECT_1),
                        name = "Block B - Ground Floor",
                        floorPlanUrl = null,
                        updatedAt = now,
                    ),
            ),
        )

    private fun project2Structures(now: Timestamp) =
        listOf(
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_4),
                        projectId = Uuid.parse(PROJECT_2),
                        name = "Main Building - Basement",
                        floorPlanUrl = null,
                        updatedAt = now,
                    ),
            ),
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_5),
                        projectId = Uuid.parse(PROJECT_2),
                        name = "Main Building - Ground Floor",
                        floorPlanUrl = "https://www.thehousedesigners.com/images/plans/01/SCA/bulk/9333/1st-floor_m.webp",
                        updatedAt = now,
                    ),
            ),
        )

    private fun project3Structures(now: Timestamp) =
        listOf(
            BackendStructure(
                structure =
                    Structure(
                        id = Uuid.parse(STRUCTURE_6),
                        projectId = Uuid.parse(PROJECT_3),
                        name = "Reading Hall - Level 1",
                        floorPlanUrl = null,
                        updatedAt = now,
                    ),
            ),
        )

    private suspend fun seedFindings() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendFinding(
                finding =
                    Finding(
                        id = Uuid.parse(FINDING_1),
                        structureId = Uuid.parse(STRUCTURE_1),
                        name = "Cracked wall tile",
                        description = "Visible crack on wall tile near entrance.",
                        importance = Importance.HIGH,
                        term = Term.T1,
                        coordinates = listOf(RelativeCoordinate(x = 0.25f, y = 0.40f)),
                        updatedAt = now,
                    ),
            ),
            BackendFinding(
                finding =
                    Finding(
                        id = Uuid.parse(FINDING_2),
                        structureId = Uuid.parse(STRUCTURE_1),
                        name = "Missing paint patch",
                        description = "Unpainted area on the ceiling in hallway.",
                        importance = Importance.MEDIUM,
                        term = Term.T2,
                        coordinates = listOf(RelativeCoordinate(x = 0.60f, y = 0.15f)),
                        updatedAt = now,
                    ),
            ),
            BackendFinding(
                finding =
                    Finding(
                        id = Uuid.parse(FINDING_3),
                        structureId = Uuid.parse(STRUCTURE_2),
                        name = "Loose handrail",
                        description = null,
                        importance = Importance.LOW,
                        term = Term.T3,
                        coordinates =
                            listOf(
                                RelativeCoordinate(x = 0.80f, y = 0.55f),
                                RelativeCoordinate(x = 0.82f, y = 0.60f),
                            ),
                        updatedAt = now,
                    ),
            ),
        ).forEach { findingsDb.saveFinding(it) }
    }

    private companion object {
        private const val PROJECT_1 = "00000000-0000-0000-0000-000000000001"
        private const val PROJECT_2 = "00000000-0000-0000-0000-000000000002"
        private const val PROJECT_3 = "00000000-0000-0000-0000-000000000003"
        private const val STRUCTURE_1 = "00000000-0000-0000-0001-000000000001"
        private const val STRUCTURE_2 = "00000000-0000-0000-0001-000000000002"
        private const val STRUCTURE_3 = "00000000-0000-0000-0001-000000000003"
        private const val STRUCTURE_4 = "00000000-0000-0000-0001-000000000004"
        private const val STRUCTURE_5 = "00000000-0000-0000-0001-000000000005"
        private const val STRUCTURE_6 = "00000000-0000-0000-0001-000000000006"
        private const val FINDING_1 = "00000000-0000-0000-0002-000000000001"
        private const val FINDING_2 = "00000000-0000-0000-0002-000000000002"
        private const val FINDING_3 = "00000000-0000-0000-0002-000000000003"
    }
}
