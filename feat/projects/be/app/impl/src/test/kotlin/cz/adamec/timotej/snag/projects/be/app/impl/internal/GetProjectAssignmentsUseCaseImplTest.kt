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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetProjectAssignmentsUseCaseImplTest : BackendKoinInitializedTest() {
    private val usersDb: UsersDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val assignmentsDb: ProjectAssignmentsDb by inject()
    private val useCase: GetProjectAssignmentsUseCase by inject()

    @Test
    fun `returns empty list when no assignments exist`() =
        runTest(testDispatcher) {
            val result = useCase(TEST_PROJECT_ID)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns assigned users`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.seedTestProject()
            val user1 =
                BackendUserData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000010"),
                    authProviderId = "entra-1",
                    email = "user1@example.com",
                    role = UserRole.ADMINISTRATOR,
                    updatedAt = Timestamp(100L),
                )
            val user2 =
                BackendUserData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000020"),
                    authProviderId = "entra-2",
                    email = "user2@example.com",
                    updatedAt = Timestamp(100L),
                )
            usersDb.saveUser(user1)
            usersDb.saveUser(user2)
            assignmentsDb.assignUser(user1.id, TEST_PROJECT_ID)
            assignmentsDb.assignUser(user2.id, TEST_PROJECT_ID)

            val result = useCase(TEST_PROJECT_ID)

            assertEquals(2, result.size)
        }
}
