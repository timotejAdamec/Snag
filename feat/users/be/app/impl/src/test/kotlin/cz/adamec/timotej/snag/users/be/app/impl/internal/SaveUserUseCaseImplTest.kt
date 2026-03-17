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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class SaveUserUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: UsersDb by inject()
    private val useCase: SaveUserUseCase by inject()

    private val userId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `saves new user`() =
        runTest(testDispatcher) {
            val user =
                BackendUser(
                    user =
                        User(
                            id = userId,
                            entraId = "entra-1",
                            email = "user@example.com",
                            role = UserRole.ADMINISTRATOR,
                            updatedAt = Timestamp(100L),
                        ),
                )

            useCase(user)

            val stored = dataSource.getUser(userId)
            assertEquals(user, stored)
        }

    @Test
    fun `updates existing user`() =
        runTest(testDispatcher) {
            val user =
                BackendUser(
                    user =
                        User(
                            id = userId,
                            entraId = "entra-1",
                            email = "user@example.com",
                            role = UserRole.ADMINISTRATOR,
                            updatedAt = Timestamp(100L),
                        ),
                )
            dataSource.saveUser(user)

            val updatedUser =
                BackendUser(
                    user =
                        User(
                            id = userId,
                            entraId = "entra-1",
                            email = "updated@example.com",
                            role = UserRole.PASSPORT_LEAD,
                            updatedAt = Timestamp(200L),
                        ),
                )

            useCase(updatedUser)

            val stored = dataSource.getUser(userId)
            assertEquals(updatedUser, stored)
        }

    @Test
    fun `saves user with null role`() =
        runTest(testDispatcher) {
            val user =
                BackendUser(
                    user =
                        User(
                            id = userId,
                            entraId = "entra-1",
                            email = "user@example.com",
                            role = null,
                            updatedAt = Timestamp(100L),
                        ),
                )

            useCase(user)

            val stored = dataSource.getUser(userId)
            assertEquals(user, stored)
        }

    @Test
    fun `returns saved user`() =
        runTest(testDispatcher) {
            val user =
                BackendUser(
                    user =
                        User(
                            id = userId,
                            entraId = "entra-1",
                            email = "user@example.com",
                            role = UserRole.SERVICE_WORKER,
                            updatedAt = Timestamp(100L),
                        ),
                )

            val result = useCase(user)

            assertEquals(user, result)
        }
}
