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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.reports.be.app.api.GenerateProjectReportUseCase
import cz.adamec.timotej.snag.reports.be.driven.test.FakePdfReportGenerator
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
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
    private val usersDb: UsersDb by inject()
    private val fakeGenerator: FakePdfReportGenerator by inject()

    @Test
    fun `returns null when project does not exist`() =
        runTest(testDispatcher) {
            val result = useCase(Uuid.parse("00000000-0000-0000-0000-000000000099"))
            assertNull(result)
        }

    @Test
    fun `generates report for existing project`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = PROJECT_ID,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )

            val result = useCase(PROJECT_ID)

            assertNotNull(result)
            assertEquals(PROJECT_ID, result.projectId)
            assertContentEquals(FakePdfReportGenerator.FAKE_PDF_BYTES, result.bytes)
            val lastData = fakeGenerator.lastData
            assertNotNull(lastData)
            assertEquals("Test Project", lastData.project.name)
        }

    @Test
    fun `filters soft-deleted structures`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = PROJECT_ID,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )
            structuresDb.saveStructure(
                BackendStructureData(
                    id = STRUCTURE_ID_1,
                    projectId = PROJECT_ID,
                    name = "Active Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(1L),
                ),
            )
            structuresDb.saveStructure(
                BackendStructureData(
                    id = STRUCTURE_ID_2,
                    projectId = PROJECT_ID,
                    name = "Deleted Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(1L),
                    deletedAt = Timestamp(2L),
                ),
            )

            useCase(PROJECT_ID)

            val lastData = fakeGenerator.lastData
            assertNotNull(lastData)
            assertEquals(1, lastData.structures.size)
            assertEquals("Active Structure", lastData.structures[0].name)
        }

    companion object {
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val STRUCTURE_ID_1 = Uuid.parse("00000000-0000-0000-0000-000000000010")
        private val STRUCTURE_ID_2 = Uuid.parse("00000000-0000-0000-0000-000000000011")
    }
}
