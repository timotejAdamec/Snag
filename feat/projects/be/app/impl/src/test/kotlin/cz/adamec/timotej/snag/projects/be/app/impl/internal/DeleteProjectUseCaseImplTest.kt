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
import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DeleteProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: DeleteProjectUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val project =
        BackendProjectData(
            id = projectId,
            name = "Test Project",
            address = "Test Address",
            creatorId = TEST_USER_ID,
            updatedAt = Timestamp(10L),
        )

    @Test
    fun `soft-deletes project in storage`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            dataSource.saveProject(project)

            useCase(DeleteProjectRequest(projectId = projectId, deletedAt = Timestamp(20L)))

            val deletedProject = dataSource.getProject(projectId)
            assertNotNull(deletedProject)
            assertEquals(Timestamp(20L), deletedProject.deletedAt)
        }

    @Test
    fun `does not delete project when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            dataSource.saveProject(project)

            useCase(
                DeleteProjectRequest(
                    projectId = projectId,
                    deletedAt = Timestamp(value = 1L),
                ),
            )

            assertNotNull(dataSource.getProject(projectId))
        }

    @Test
    fun `returns saved project when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            dataSource.saveProject(project)

            val result =
                useCase(
                    DeleteProjectRequest(
                        projectId = projectId,
                        deletedAt = Timestamp(value = 1L),
                    ),
                )

            assertNotNull(result)
            assertEquals(project, result)
        }

    @Test
    fun `returns null if no project was saved`() =
        runTest(testDispatcher) {
            val result =
                useCase(
                    DeleteProjectRequest(
                        projectId = projectId,
                        deletedAt = Timestamp(value = 20L),
                    ),
                )

            assertNull(result)
        }
}
