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
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
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
class CanCloseProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val useCase: CanCloseProjectUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")
    private val projectId = UuidProvider.getUuid()

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

    private fun seedProject(creatorId: Uuid = currentUserId) {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Address",
                creatorId = creatorId,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    @Test
    fun `returns true for administrator on any project`() =
        runTest(testDispatcher) {
            val otherCreator = UuidProvider.getUuid()
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            seedProject(creatorId = otherCreator)

            assertTrue(useCase(projectId).first())
        }

    @Test
    fun `returns true for project creator`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)
            seedProject(creatorId = currentUserId)

            assertTrue(useCase(projectId).first())
        }

    @Test
    fun `returns false for non-admin non-creator`() =
        runTest(testDispatcher) {
            val otherCreator = UuidProvider.getUuid()
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)
            seedProject(creatorId = otherCreator)

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when project not found`() =
        runTest(testDispatcher) {
            seedCurrentUser()

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when user not found`() =
        runTest(testDispatcher) {
            seedProject()

            assertFalse(useCase(projectId).first())
        }
}
