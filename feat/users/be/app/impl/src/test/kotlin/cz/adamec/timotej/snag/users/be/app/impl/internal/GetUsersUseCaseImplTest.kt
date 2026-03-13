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

package cz.adamec.timotej.snag.users.be.app.impl.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetUsersUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: UsersDb by inject()
    private val useCase: GetUsersUseCase by inject()

    @Test
    fun `returns empty list when none exist`() =
        runTest(testDispatcher) {
            val result = useCase()

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns all users`() =
        runTest(testDispatcher) {
            val user1 =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                            entraId = "entra-1",
                            email = "user1@example.com",
                            role = UserRole.ADMINISTRATOR,
                            updatedAt = Timestamp(100L),
                        ),
                )
            val user2 =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                            entraId = "entra-2",
                            email = "user2@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                )
            dataSource.saveUser(user1)
            dataSource.saveUser(user2)

            val result = useCase()

            assertEquals(2, result.size)
            assertEquals(user1, result.find { it.user.id == user1.user.id })
            assertEquals(user2, result.find { it.user.id == user2.user.id })
        }
}
