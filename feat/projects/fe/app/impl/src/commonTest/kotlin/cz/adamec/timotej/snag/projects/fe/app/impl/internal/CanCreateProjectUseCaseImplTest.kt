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
import cz.adamec.timotej.snag.projects.fe.app.api.CanCreateProjectUseCase
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
class CanCreateProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersDb: FakeUsersDb by inject()
    private val useCase: CanCreateProjectUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun seedCurrentUser(role: UserRole?) {
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

    @Test
    fun `returns true for administrator`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            assertTrue(useCase().first())
        }

    @Test
    fun `returns true for passport lead`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)
            assertTrue(useCase().first())
        }

    @Test
    fun `returns true for service lead`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_LEAD)
            assertTrue(useCase().first())
        }

    @Test
    fun `returns true for service worker`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_WORKER)
            assertTrue(useCase().first())
        }

    @Test
    fun `returns false for passport technician`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_TECHNICIAN)
            assertFalse(useCase().first())
        }

    @Test
    fun `returns false for user with no role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = null)
            assertFalse(useCase().first())
        }

    @Test
    fun `returns false when user not found`() =
        runTest(testDispatcher) {
            assertFalse(useCase().first())
        }
}
