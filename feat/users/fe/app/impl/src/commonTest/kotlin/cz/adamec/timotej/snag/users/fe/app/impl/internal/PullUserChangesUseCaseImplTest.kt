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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakePullSyncTimestampDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.fe.app.api.PullUserChangesUseCase
import cz.adamec.timotej.snag.users.fe.app.impl.internal.sync.USER_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersApi
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UserSyncResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class PullUserChangesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersApi: FakeUsersApi by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakePullSyncTimestampDb: FakePullSyncTimestampDb by inject()

    private val useCase: PullUserChangesUseCase by inject()

    private val userId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun createUser(id: Uuid) =
        FrontendUser(
            user =
                User(
                    id = id,
                    entraId = "entra-1",
                    email = "user@example.com",
                    updatedAt = Timestamp(100L),
                ),
        )

    @Test
    fun `saves updated users to db`() =
        runTest(testDispatcher) {
            val user = createUser(userId)
            fakeUsersApi.modifiedSinceResults =
                listOf(
                    UserSyncResult.Updated(user = user),
                )

            useCase()

            val result = fakeUsersDb.getUserFlow(userId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendUser?>>(result)
            assertNotNull(result.data)
            assertEquals(userId, result.data!!.user.id)
        }

    @Test
    fun `stores last synced timestamp on success`() =
        runTest(testDispatcher) {
            fakeUsersApi.modifiedSinceResults = emptyList()

            useCase()

            assertNotNull(fakePullSyncTimestampDb.getLastSyncedAt(USER_SYNC_ENTITY_TYPE, ""))
        }

    @Test
    fun `does not store timestamp on API failure`() =
        runTest(testDispatcher) {
            fakeUsersApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase()

            assertNull(fakePullSyncTimestampDb.getLastSyncedAt(USER_SYNC_ENTITY_TYPE, ""))
        }
}
