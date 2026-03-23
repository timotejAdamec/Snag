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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetProjectsModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: GetProjectsModifiedSinceUseCase by inject()

    @Test
    fun `returns empty list when no projects exist`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()

            val result = useCase(userId = TEST_USER_ID, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns projects with updatedAt after since`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val project =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Project 1",
                    address = "Address 1",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveProject(project)

            val result = useCase(userId = TEST_USER_ID, since = Timestamp(100L))

            assertEquals(listOf(project), result)
        }

    @Test
    fun `excludes projects with updatedAt before since`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val project =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Project 1",
                    address = "Address 1",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(50L),
                )
            dataSource.saveProject(project)

            val result = useCase(userId = TEST_USER_ID, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted projects when deletedAt is after since`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val project =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Project 1",
                    address = "Address 1",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveProject(project)

            val result = useCase(userId = TEST_USER_ID, since = Timestamp(100L))

            assertEquals(listOf(project), result)
        }

    @Test
    fun `excludes deleted projects when deletedAt is before since`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val project =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Project 1",
                    address = "Address 1",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(80L),
                )
            dataSource.saveProject(project)

            val result = useCase(userId = TEST_USER_ID, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
