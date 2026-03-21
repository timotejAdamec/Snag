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
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class SaveStructureUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: SaveStructureUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val backendStructure =
        BackendStructureData(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            projectId = projectId,
            name = "Ground Floor",
            floorPlanUrl = null,
            updatedAt = Timestamp(value = 10L),
        )

    private suspend fun seedTestUser() {
        usersDb.saveUser(
            BackendUserData(
                id = TEST_USER_ID,
                entraId = "test-entra",
                email = "test@example.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    private fun createProject() =
        runTest(testDispatcher) {
            seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )
        }

    @Test
    fun `saves structure to data source`() =
        runTest(testDispatcher) {
            createProject()
            useCase(backendStructure)

            assertEquals(listOf(backendStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `does not save structure if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            createProject()
            val savedStructure =
                backendStructure.copy(
                    updatedAt = Timestamp(value = 20L),
                )
            dataSource.saveStructure(savedStructure)

            useCase(backendStructure)

            assertEquals(listOf(savedStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `returns null if structure was not present`() =
        runTest(testDispatcher) {
            createProject()
            val result = useCase(backendStructure)

            assertNull(result)
        }

    @Test
    fun `returns saved structure if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            createProject()
            val savedStructure =
                backendStructure.copy(
                    updatedAt = Timestamp(value = 20L),
                )
            dataSource.saveStructure(savedStructure)

            val result = useCase(backendStructure)

            assertEquals(savedStructure, result)
        }

    @Test
    fun `returns null if saved updated at is earlier than the new one`() =
        runTest(testDispatcher) {
            createProject()
            dataSource.saveStructure(backendStructure)

            val newerStructure =
                backendStructure.copy(
                    name = "New name",
                    updatedAt = Timestamp(value = 20L),
                )

            val result = useCase(newerStructure)

            assertNull(result)
        }

    private fun createClosedProject() =
        runTest(testDispatcher) {
            seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    creatorId = TEST_USER_ID,
                    isClosed = true,
                    updatedAt = Timestamp(1L),
                ),
            )
        }

    @Test
    fun `returns existing entity when project is closed`() =
        runTest(testDispatcher) {
            createClosedProject()
            dataSource.saveStructure(backendStructure)

            val newStructure =
                backendStructure.copy(
                    name = "Updated name",
                    updatedAt = Timestamp(value = 20L),
                )

            val result = useCase(newStructure)

            assertEquals(backendStructure, result)
            assertEquals(listOf(backendStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `returns null when project is closed and entity not in DB`() =
        runTest(testDispatcher) {
            createClosedProject()

            val result = useCase(backendStructure)

            assertNull(result)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
