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

package cz.adamec.timotej.snag.reports.be.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.reports.be.app.api.GenerateProjectReportUseCase
import cz.adamec.timotej.snag.reports.be.driven.test.FakePdfReportGenerator
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GenerateProjectReportUseCaseImplTest : BackendKoinInitializedTest() {
    private val useCase: GenerateProjectReportUseCase by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val fakeGenerator: FakePdfReportGenerator by inject()

    @Test
    fun `returns null when project does not exist`() =
        runTest {
            val result = useCase(Uuid.parse("00000000-0000-0000-0000-000000000099"))
            assertNull(result)
        }

    @Test
    fun `generates report for existing project`() =
        runTest {
            projectsDb.saveProject(
                BackendProject(
                    project =
                        Project(
                            id = PROJECT_ID,
                            name = "Test Project",
                            address = "Test Address",
                            updatedAt = Timestamp(1L),
                        ),
                ),
            )

            val result = useCase(PROJECT_ID)

            assertNotNull(result)
            assertEquals(PROJECT_ID, result.report.projectId)
            assertContentEquals(FakePdfReportGenerator.FAKE_PDF_BYTES, result.report.bytes)
            val lastData = fakeGenerator.lastData
            assertNotNull(lastData)
            assertEquals("Test Project", lastData.project.project.name)
        }

    @Test
    fun `filters soft-deleted structures`() =
        runTest {
            projectsDb.saveProject(
                BackendProject(
                    project =
                        Project(
                            id = PROJECT_ID,
                            name = "Test Project",
                            address = "Test Address",
                            updatedAt = Timestamp(1L),
                        ),
                ),
            )
            structuresDb.saveStructure(
                BackendStructure(
                    structure =
                        Structure(
                            id = STRUCTURE_ID_1,
                            projectId = PROJECT_ID,
                            name = "Active Structure",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(1L),
                        ),
                ),
            )
            structuresDb.saveStructure(
                BackendStructure(
                    structure =
                        Structure(
                            id = STRUCTURE_ID_2,
                            projectId = PROJECT_ID,
                            name = "Deleted Structure",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(1L),
                        ),
                    deletedAt = Timestamp(2L),
                ),
            )

            useCase(PROJECT_ID)

            val lastData = fakeGenerator.lastData
            assertNotNull(lastData)
            assertEquals(1, lastData.structures.size)
            assertEquals("Active Structure", lastData.structures[0].structure.name)
        }

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val STRUCTURE_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val STRUCTURE_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000011")
    }
}
