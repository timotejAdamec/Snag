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
import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanAccessProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val useCase: CanAccessProjectUseCase by inject()
    private val usersDb: UsersDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val assignmentsDb: ProjectAssignmentsDb by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `service lead can access project created by service worker`() =
        runTest(testDispatcher) {
            val serviceLeadId = Uuid.parse("00000000-0000-0000-0000-000000000010")
            val serviceWorkerId = Uuid.parse("00000000-0000-0000-0000-000000000011")
            usersDb.saveUser(
                BackendUserData(
                    id = serviceLeadId,
                    authProviderId = "auth-provider-sl",
                    email = "sl@example.com",
                    role = UserRole.SERVICE_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            usersDb.saveUser(
                BackendUserData(
                    id = serviceWorkerId,
                    authProviderId = "auth-provider-sw",
                    email = "sw@example.com",
                    role = UserRole.SERVICE_WORKER,
                    updatedAt = Timestamp(1L),
                ),
            )
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Worker Project",
                    address = "Address",
                    creatorId = serviceWorkerId,
                    updatedAt = Timestamp(10L),
                ),
            )

            val result =
                useCase(
                    userId = serviceLeadId,
                    projectId = projectId,
                )

            assertTrue(result)
        }

    @Test
    fun `service lead cannot access project created by passport lead`() =
        runTest(testDispatcher) {
            val serviceLeadId = Uuid.parse("00000000-0000-0000-0000-000000000010")
            val passportLeadId = Uuid.parse("00000000-0000-0000-0000-000000000012")
            usersDb.saveUser(
                BackendUserData(
                    id = serviceLeadId,
                    authProviderId = "auth-provider-sl",
                    email = "sl@example.com",
                    role = UserRole.SERVICE_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            usersDb.saveUser(
                BackendUserData(
                    id = passportLeadId,
                    authProviderId = "auth-provider-pl",
                    email = "pl@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Passport Project",
                    address = "Address",
                    creatorId = passportLeadId,
                    updatedAt = Timestamp(10L),
                ),
            )

            val result =
                useCase(
                    userId = serviceLeadId,
                    projectId = projectId,
                )

            assertFalse(result)
        }

    @Test
    fun `returns false when user does not exist`() =
        runTest(testDispatcher) {
            val unknownId = Uuid.parse("00000000-0000-0000-0000-000000000099")

            val result =
                useCase(
                    userId = unknownId,
                    projectId = projectId,
                )

            assertFalse(result)
        }

    @Test
    fun `returns false when project does not exist`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            val unknownProjectId = Uuid.parse("00000000-0000-0000-0000-000000000099")

            val result =
                useCase(
                    userId = cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID,
                    projectId = unknownProjectId,
                )

            assertFalse(result)
        }

    @Test
    fun `creator can access own project`() =
        runTest(testDispatcher) {
            val passportLeadId = Uuid.parse("00000000-0000-0000-0000-000000000020")
            usersDb.saveUser(
                BackendUserData(
                    id = passportLeadId,
                    authProviderId = "auth-provider-pl",
                    email = "pl@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Own Project",
                    address = "Address",
                    creatorId = passportLeadId,
                    updatedAt = Timestamp(10L),
                ),
            )

            val result =
                useCase(
                    userId = passportLeadId,
                    projectId = projectId,
                )

            assertTrue(result)
        }

    @Test
    fun `assigned user can access project`() =
        runTest(testDispatcher) {
            val technicianId = Uuid.parse("00000000-0000-0000-0000-000000000030")
            val creatorId = Uuid.parse("00000000-0000-0000-0000-000000000031")
            usersDb.saveUser(
                BackendUserData(
                    id = technicianId,
                    authProviderId = "auth-provider-pt",
                    email = "pt@example.com",
                    role = UserRole.PASSPORT_TECHNICIAN,
                    updatedAt = Timestamp(1L),
                ),
            )
            usersDb.saveUser(
                BackendUserData(
                    id = creatorId,
                    authProviderId = "auth-provider-creator",
                    email = "creator@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(1L),
                ),
            )
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Assigned Project",
                    address = "Address",
                    creatorId = creatorId,
                    updatedAt = Timestamp(10L),
                ),
            )
            assignmentsDb.assignUser(
                userId = technicianId,
                projectId = projectId,
            )

            val result =
                useCase(
                    userId = technicianId,
                    projectId = projectId,
                )

            assertTrue(result)
        }
}
