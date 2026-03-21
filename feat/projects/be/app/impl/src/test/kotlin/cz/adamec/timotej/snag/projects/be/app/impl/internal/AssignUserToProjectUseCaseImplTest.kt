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
import cz.adamec.timotej.snag.projects.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.AssignUserToProjectRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class AssignUserToProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val usersDb: UsersDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val assignmentsDb: ProjectAssignmentsDb by inject()
    private val useCase: AssignUserToProjectUseCase by inject()

    private val userId = Uuid.parse("00000000-0000-0000-0000-000000000010")
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

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

    private suspend fun createProject() {
        seedTestUser()
        projectsDb.saveProject(
            BackendProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
                updatedAt = Timestamp(10L),
            ),
        )
    }

    @Test
    fun `assigns user to project`() =
        runTest(testDispatcher) {
            createProject()
            val user =
                BackendUserData(
                    id = userId,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                )
            usersDb.saveUser(user)

            useCase(AssignUserToProjectRequest(userId = userId, projectId = projectId))

            val assigned = assignmentsDb.getAssignedUsers(projectId)
            assertEquals(1, assigned.size)
            assertEquals(userId, assigned[0].id)
        }

    @Test
    fun `idempotent re-assignment does not duplicate`() =
        runTest(testDispatcher) {
            createProject()
            val user =
                BackendUserData(
                    id = userId,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                )
            usersDb.saveUser(user)

            useCase(AssignUserToProjectRequest(userId = userId, projectId = projectId))
            useCase(AssignUserToProjectRequest(userId = userId, projectId = projectId))

            val assigned = assignmentsDb.getAssignedUsers(projectId)
            assertEquals(1, assigned.size)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
