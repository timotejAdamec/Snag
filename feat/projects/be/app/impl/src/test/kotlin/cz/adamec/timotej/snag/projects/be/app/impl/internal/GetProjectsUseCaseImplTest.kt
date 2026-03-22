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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetProjectsUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: GetProjectsUseCase by inject()

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
    fun `returns empty list when none exist`() =
        runTest(testDispatcher) {
            val result = useCase()

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns all projects`() =
        runTest(testDispatcher) {
            seedTestUser()
            val project1 =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Project 1",
                    address = "Address 1",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(10L),
                )
            val project2 =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                    name = "Project 2",
                    address = "Address 2",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(10L),
                )
            dataSource.saveProject(project1)
            dataSource.saveProject(project2)

            val result = useCase()

            assertEquals(listOf(project1, project2), result)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
