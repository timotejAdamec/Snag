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

package cz.adamec.timotej.snag.structures.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.DeleteStructureRequest
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DeleteStructureUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: DeleteStructureUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structure =
        BackendStructureData(
            id = structureId,
            projectId = TEST_PROJECT_ID,
            name = "Ground Floor",
            floorPlanUrl = null,
            updatedAt = Timestamp(value = 10L),
        )

    private fun createProject() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.seedTestProject()
        }

    @Test
    fun `soft-deletes structure in storage`() =
        runTest(testDispatcher) {
            createProject()
            dataSource.saveStructure(structure)

            useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 20L),
                ),
            )

            val deletedStructure =
                dataSource.getStructures(TEST_PROJECT_ID).find { it.id == structureId }
            assertNotNull(deletedStructure)
            assertEquals(Timestamp(20L), deletedStructure.deletedAt)
        }

    @Test
    fun `does not delete structure when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            createProject()
            dataSource.saveStructure(structure)

            useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 1L),
                ),
            )

            assertNotNull(
                dataSource.getStructures(TEST_PROJECT_ID).find { it.id == structureId },
            )
        }

    @Test
    fun `returns saved structure when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            createProject()
            dataSource.saveStructure(structure)

            val result =
                useCase(
                    DeleteStructureRequest(
                        structureId = structureId,
                        deletedAt = Timestamp(value = 1L),
                    ),
                )

            assertNotNull(result)
            assertEquals(structure, result)
        }

    @Test
    fun `returns null if no structure was saved`() =
        runTest(testDispatcher) {
            val result =
                useCase(
                    DeleteStructureRequest(
                        structureId = structureId,
                        deletedAt = Timestamp(value = 20L),
                    ),
                )

            assertNull(result)
        }

    private fun createClosedProject() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.seedTestProject(isClosed = true)
        }

    @Test
    fun `returns existing entity when project is closed`() =
        runTest(testDispatcher) {
            createClosedProject()
            dataSource.saveStructure(structure)

            val result =
                useCase(
                    DeleteStructureRequest(
                        structureId = structureId,
                        deletedAt = Timestamp(value = 20L),
                    ),
                )

            assertEquals(structure, result)
            val stored =
                dataSource.getStructures(TEST_PROJECT_ID).find { it.id == structureId }
            assertNull(stored?.deletedAt)
        }
}
