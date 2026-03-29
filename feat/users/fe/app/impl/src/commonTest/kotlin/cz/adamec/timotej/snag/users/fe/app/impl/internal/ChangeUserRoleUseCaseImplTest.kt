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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.model.ChangeUserRoleRequest
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersApi
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class ChangeUserRoleUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersApi: FakeUsersApi by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
    private val useCase: ChangeUserRoleUseCase by inject()

    private val testUser =
        AppUserData(
            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
            authProviderId = "entra-1",
            email = "user@example.com",
            role = null,
            updatedAt = Timestamp(100L),
        )

    @Test
    fun `updates user role via API and saves to DB`() =
        runTest(testDispatcher) {
            fakeUsersApi.setUser(testUser)
            fakeUsersDb.setUser(testUser)

            val result = useCase(ChangeUserRoleRequest(testUser.id, UserRole.ADMINISTRATOR))

            assertIs<OnlineDataResult.Success<AppUser>>(result)
            assertEquals(UserRole.ADMINISTRATOR, result.data.role)

            val dbResult = fakeUsersDb.getUserFlow(testUser.id).first()
            assertIs<OfflineFirstDataResult.Success<AppUser?>>(dbResult)
            assertEquals(UserRole.ADMINISTRATOR, dbResult.data?.role)
        }

    @Test
    fun `clears user role when null is passed`() =
        runTest(testDispatcher) {
            val userWithRole = testUser.copy(role = UserRole.SERVICE_LEAD)
            fakeUsersApi.setUser(userWithRole)
            fakeUsersDb.setUser(userWithRole)

            val result = useCase(ChangeUserRoleRequest(userWithRole.id, null))

            assertIs<OnlineDataResult.Success<AppUser>>(result)
            assertEquals(null, result.data.role)
        }

    @Test
    fun `returns failure and does not save to DB on API error`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)
            fakeUsersApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = useCase(ChangeUserRoleRequest(testUser.id, UserRole.PASSPORT_LEAD))

            assertIs<OnlineDataResult.Failure>(result)
        }
}
