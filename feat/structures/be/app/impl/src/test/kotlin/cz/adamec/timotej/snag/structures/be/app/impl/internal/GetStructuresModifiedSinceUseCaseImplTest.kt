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
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresModifiedSinceUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.GetStructuresModifiedSinceRequest
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetStructuresModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: GetStructuresModifiedSinceUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

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

    @Test
    fun `returns empty list when no structures exist`() =
        runTest(testDispatcher) {
            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns structures with updatedAt after since`() =
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
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes structures from different project`() =
        runTest(testDispatcher) {
            seedTestUser()
            projectsDb.saveProject(
                BackendProjectData(
                    id = otherProjectId,
                    name = "Other Project",
                    address = "Other Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(1L),
                ),
            )
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = otherProjectId,
                    name = "Other Building",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted structures when deletedAt is after since`() =
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
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes unchanged structures`() =
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
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(50L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
