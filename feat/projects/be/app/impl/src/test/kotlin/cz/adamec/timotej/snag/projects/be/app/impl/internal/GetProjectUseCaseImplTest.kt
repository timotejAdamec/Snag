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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: GetProjectUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val project =
        BackendProjectData(
            id = projectId,
            name = "Test Project",
            address = "Test Address",
            creatorId = TEST_USER_ID,
            updatedAt = Timestamp(10L),
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

    @Test
    fun `returns project when it exists`() =
        runTest(testDispatcher) {
            seedTestUser()
            dataSource.saveProject(project)

            val result = useCase(projectId)

            assertEquals(project, result)
        }

    @Test
    fun `returns null when not found`() =
        runTest(testDispatcher) {
            val result = useCase(projectId)

            assertNull(result)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
