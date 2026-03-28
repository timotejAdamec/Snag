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
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class CanEditProjectEntitiesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val useCase: CanEditProjectEntitiesUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")
    private val projectId = UuidProvider.getUuid()

    private fun seedCurrentUser(role: UserRole? = UserRole.PASSPORT_LEAD) {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                entraId = "entra-id",
                email = "user@example.com",
                role = role,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun seedProject(
        isClosed: Boolean = false,
        creatorId: Uuid = currentUserId,
    ) {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Address",
                creatorId = creatorId,
                isClosed = isClosed,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    @Test
    fun `returns true for open project where user is creator`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            seedProject(isClosed = false, creatorId = currentUserId)

            val result = useCase(projectId).first()

            assertTrue(result)
        }

    @Test
    fun `returns false for closed project even when user is creator`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            seedProject(isClosed = true, creatorId = currentUserId)

            val result = useCase(projectId).first()

            assertFalse(result)
        }

    @Test
    fun `returns true for open project where user is assigned`() =
        runTest(testDispatcher) {
            val otherCreator = UuidProvider.getUuid()
            seedCurrentUser()
            seedProject(isClosed = false, creatorId = otherCreator)
            fakeProjectAssignmentsDb.setAssignments(projectId, setOf(currentUserId))

            val result = useCase(projectId).first()

            assertTrue(result)
        }

    @Test
    fun `returns false for open project where user has no access`() =
        runTest(testDispatcher) {
            val otherCreator = UuidProvider.getUuid()
            seedCurrentUser(role = UserRole.PASSPORT_TECHNICIAN)
            seedProject(isClosed = false, creatorId = otherCreator)

            val result = useCase(projectId).first()

            assertFalse(result)
        }

    @Test
    fun `returns true for open project when user is administrator`() =
        runTest(testDispatcher) {
            val otherCreator = UuidProvider.getUuid()
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            seedProject(isClosed = false, creatorId = otherCreator)

            val result = useCase(projectId).first()

            assertTrue(result)
        }

    @Test
    fun `returns false when project not found`() =
        runTest(testDispatcher) {
            seedCurrentUser()

            val result = useCase(projectId).first()

            assertFalse(result)
        }

    @Test
    fun `returns false when user not found`() =
        runTest(testDispatcher) {
            seedProject()

            val result = useCase(projectId).first()

            assertFalse(result)
        }
}
