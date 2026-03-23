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
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
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
    private val assignmentsDb: ProjectAssignmentsDb by inject()
    private val useCase: GetProjectsUseCase by inject()

    @Test
    fun `returns empty list when none exist`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()

            val result = useCase(TEST_USER_ID)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `admin returns all projects`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
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

            val result = useCase(TEST_USER_ID)

            assertEquals(listOf(project1, project2), result)
        }

    @Test
    fun `non-admin returns only own and assigned projects`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val leadId = Uuid.parse("00000000-0000-0000-0000-000000000098")
            usersDb.saveUser(
                BackendUserData(
                    id = leadId,
                    entraId = "lead-entra",
                    email = "lead@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            val ownProject =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Lead's Project",
                    address = "Address",
                    creatorId = leadId,
                    updatedAt = Timestamp(10L),
                )
            val assignedProject =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                    name = "Assigned Project",
                    address = "Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(10L),
                )
            val otherProject =
                BackendProjectData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000003"),
                    name = "Other Project",
                    address = "Address",
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(10L),
                )
            dataSource.saveProject(ownProject)
            dataSource.saveProject(assignedProject)
            dataSource.saveProject(otherProject)
            assignmentsDb.assignUser(
                userId = leadId,
                projectId = assignedProject.id,
            )

            val result = useCase(leadId)

            assertEquals(2, result.size)
            assertEquals(
                setOf("Lead's Project", "Assigned Project"),
                result.map { it.name }.toSet(),
            )
        }

    @Test
    fun `returns empty list for unknown user`() =
        runTest(testDispatcher) {
            val unknownId = Uuid.parse("00000000-0000-0000-0000-000000000099")

            val result = useCase(unknownId)

            assertEquals(emptyList(), result)
        }
}
