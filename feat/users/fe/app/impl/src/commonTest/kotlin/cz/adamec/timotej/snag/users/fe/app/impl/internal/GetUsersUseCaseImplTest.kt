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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class GetUsersUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeUsersDb: FakeUsersDb by inject()
    private val useCase: GetUsersUseCase by inject()

    @Test
    fun `emits users from db flow`() =
        runTest(testDispatcher) {
            val user =
                FrontendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                )
            fakeUsersDb.setUser(user)

            val result = useCase().first()

            assertIs<OfflineFirstDataResult.Success<List<FrontendUser>>>(result)
            assertEquals(1, result.data.size)
            assertEquals(user, result.data[0])
        }

    @Test
    fun `emits empty list when no users`() =
        runTest(testDispatcher) {
            val result = useCase().first()

            assertIs<OfflineFirstDataResult.Success<List<FrontendUser>>>(result)
            assertEquals(emptyList(), result.data)
        }
}
