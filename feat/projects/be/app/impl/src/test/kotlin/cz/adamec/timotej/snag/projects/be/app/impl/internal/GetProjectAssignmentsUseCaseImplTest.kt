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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
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

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private suspend fun createProject() {
        projectsDb.saveProject(
            BackendProject(
                project =
                    Project(
                        id = projectId,
                        name = "Test Project",
                        address = "Test Address",
                        updatedAt = Timestamp(10L),
                    ),
            ),
        )
    }

    @Test
    fun `returns empty list when no assignments exist`() =
        runTest(testDispatcher) {
            val result = useCase(projectId)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns assigned users`() =
        runTest(testDispatcher) {
            createProject()
            val user1 =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000010"),
                            entraId = "entra-1",
                            email = "user1@example.com",
                            role = UserRole.ADMINISTRATOR,
                            updatedAt = Timestamp(100L),
                        ),
                )
            val user2 =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000020"),
                            entraId = "entra-2",
                            email = "user2@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                )
            usersDb.saveUser(user1)
            usersDb.saveUser(user2)
            assignmentsDb.assignUser(user1.user.id, projectId)
            assignmentsDb.assignUser(user2.user.id, projectId)

            val result = useCase(projectId)

            assertEquals(2, result.size)
        }
}
