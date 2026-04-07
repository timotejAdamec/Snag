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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetProjectsUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val useCase: GetProjectsUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")

    private fun seedCurrentUser(role: UserRole? = UserRole.PASSPORT_LEAD) {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                authProviderId = "mock-auth-provider-id",
                email = "user@example.com",
                role = role,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun seedUser(
        id: Uuid,
        role: UserRole?,
    ) {
        fakeUsersDb.setUser(
            AppUserData(
                id = id,
                authProviderId = "auth-$id",
                email = "$id@example.com",
                role = role,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun seedProject(
        id: Uuid = UuidProvider.getUuid(),
        name: String = "Project",
        creatorId: Uuid = currentUserId,
    ) {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = id,
                name = name,
                address = "Address",
                creatorId = creatorId,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private suspend fun getProjectNames(): Set<String> {
        val result = useCase().first()
        assertIs<OfflineFirstDataResult.Success<List<AppProject>>>(result)
        return result.data.map { it.name }.toSet()
    }

    @Test
    fun `admin sees all projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            val otherCreator = UuidProvider.getUuid()
            seedUser(id = otherCreator, role = UserRole.PASSPORT_LEAD)
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(name = "Other Project", creatorId = otherCreator)

            assertEquals(
                setOf("Own Project", "Other Project"),
                getProjectNames(),
            )
        }

    @Test
    fun `service worker sees only own projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_WORKER)
            val otherCreator = UuidProvider.getUuid()
            seedUser(id = otherCreator, role = UserRole.PASSPORT_LEAD)
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(name = "Other Project", creatorId = otherCreator)

            assertEquals(
                setOf("Own Project"),
                getProjectNames(),
            )
        }

    @Test
    fun `service worker sees assigned projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_WORKER)
            val otherCreator = UuidProvider.getUuid()
            seedUser(id = otherCreator, role = UserRole.PASSPORT_LEAD)
            val assignedProjectId = UuidProvider.getUuid()
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(
                id = assignedProjectId,
                name = "Assigned Project",
                creatorId = otherCreator,
            )
            fakeProjectAssignmentsDb.setAssignments(assignedProjectId, setOf(currentUserId))

            assertEquals(
                setOf("Own Project", "Assigned Project"),
                getProjectNames(),
            )
        }

    @Test
    fun `service lead sees service worker projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_LEAD)
            val serviceWorkerId = UuidProvider.getUuid()
            val passportLeadId = UuidProvider.getUuid()
            seedUser(id = serviceWorkerId, role = UserRole.SERVICE_WORKER)
            seedUser(id = passportLeadId, role = UserRole.PASSPORT_LEAD)
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(name = "Worker Project", creatorId = serviceWorkerId)
            seedProject(name = "Passport Lead Project", creatorId = passportLeadId)

            assertEquals(
                setOf("Own Project", "Worker Project"),
                getProjectNames(),
            )
        }

    @Test
    fun `passport lead does not see service worker projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)
            val serviceWorkerId = UuidProvider.getUuid()
            seedUser(id = serviceWorkerId, role = UserRole.SERVICE_WORKER)
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(name = "Worker Project", creatorId = serviceWorkerId)

            assertEquals(
                setOf("Own Project"),
                getProjectNames(),
            )
        }

    @Test
    fun `returns empty list when no user is logged in`() =
        runTest(testDispatcher) {
            seedProject(name = "Some Project", creatorId = UuidProvider.getUuid())

            assertEquals(
                emptySet(),
                getProjectNames(),
            )
        }

    @Test
    fun `passport technician sees only own and assigned projects`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_TECHNICIAN)
            val otherCreator = UuidProvider.getUuid()
            seedUser(id = otherCreator, role = UserRole.PASSPORT_LEAD)
            val assignedProjectId = UuidProvider.getUuid()
            seedProject(name = "Own Project", creatorId = currentUserId)
            seedProject(
                id = assignedProjectId,
                name = "Assigned Project",
                creatorId = otherCreator,
            )
            seedProject(name = "Unrelated Project", creatorId = otherCreator)
            fakeProjectAssignmentsDb.setAssignments(assignedProjectId, setOf(currentUserId))

            assertEquals(
                setOf("Own Project", "Assigned Project"),
                getProjectNames(),
            )
        }
}
