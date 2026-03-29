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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.GetAllowedRoleOptionsUseCase
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllowedRoleOptionsUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersDb: FakeUsersDb by inject()
    private val useCase: GetAllowedRoleOptionsUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")

    private fun seedCurrentUser(role: UserRole?) {
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

    @Test
    fun `administrator can assign any role to user with no role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)

            val result = useCase(targetCurrentRole = null).first()

            assertEquals(
                expected = UserRole.entries.toSet<UserRole?>() + null,
                actual = result,
            )
        }

    @Test
    fun `administrator can assign any role or remove role from any user`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)

            val result = useCase(targetCurrentRole = UserRole.SERVICE_WORKER).first()

            assertEquals(
                expected = UserRole.entries.toSet<UserRole?>() + null,
                actual = result,
            )
        }

    @Test
    fun `passport lead can assign passport technician to user with no role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)

            val result = useCase(targetCurrentRole = null).first()

            assertEquals(
                expected = setOf(UserRole.PASSPORT_TECHNICIAN),
                actual = result,
            )
        }

    @Test
    fun `passport lead can remove role from passport technician`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)

            val result = useCase(targetCurrentRole = UserRole.PASSPORT_TECHNICIAN).first()

            assertEquals(
                expected = setOf(null),
                actual = result,
            )
        }

    @Test
    fun `passport lead cannot change service worker role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)

            val result = useCase(targetCurrentRole = UserRole.SERVICE_WORKER).first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `service lead can assign service worker to user with no role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_LEAD)

            val result = useCase(targetCurrentRole = null).first()

            assertEquals(
                expected = setOf(UserRole.SERVICE_WORKER),
                actual = result,
            )
        }

    @Test
    fun `service lead can remove role from service worker`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_LEAD)

            val result = useCase(targetCurrentRole = UserRole.SERVICE_WORKER).first()

            assertEquals(
                expected = setOf(null),
                actual = result,
            )
        }

    @Test
    fun `passport technician cannot change any roles`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_TECHNICIAN)

            val result = useCase(targetCurrentRole = null).first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns empty set when user not found`() =
        runTest(testDispatcher) {
            val result = useCase(targetCurrentRole = null).first()

            assertTrue(result.isEmpty())
        }
}
