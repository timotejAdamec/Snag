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

import app.cash.turbine.test
import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class NonWebCanModifyProjectFilesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
    private val useCase: CanModifyProjectFilesUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")
    private val projectId = UuidProvider.getUuid()

    private fun seedEditableProject() {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                authProviderId = "mock-auth-provider-id",
                email = "user@example.com",
                role = UserRole.PASSPORT_LEAD,
                updatedAt = Timestamp(100L),
            ),
        )
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Address",
                creatorId = currentUserId,
                isClosed = false,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    @Test
    fun `returns true when can edit`() =
        runTest(testDispatcher) {
            seedEditableProject()

            useCase(projectId).test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `returns false when cannot edit`() =
        runTest(testDispatcher) {
            // No project seeded → canEditProjectEntities returns false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }
}
