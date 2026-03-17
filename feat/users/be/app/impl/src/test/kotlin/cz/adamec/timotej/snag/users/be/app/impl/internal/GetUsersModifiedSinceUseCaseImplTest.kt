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
import cz.adamec.timotej.snag.users.be.app.api.GetUsersModifiedSinceUseCase
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetUsersModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: UsersDb by inject()
    private val useCase: GetUsersModifiedSinceUseCase by inject()

    @Test
    fun `returns empty list when no users modified since`() =
        runTest(testDispatcher) {
            dataSource.saveUser(
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                            entraId = "entra-1",
                            email = "user@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                ),
            )

            val result = useCase(Timestamp(200L))

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns users modified since timestamp`() =
        runTest(testDispatcher) {
            val oldUser =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                            entraId = "entra-1",
                            email = "old@example.com",
                            updatedAt = Timestamp(100L),
                        ),
                )
            val newUser =
                BackendUser(
                    user =
                        User(
                            id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                            entraId = "entra-2",
                            email = "new@example.com",
                            role = UserRole.ADMINISTRATOR,
                            updatedAt = Timestamp(300L),
                        ),
                )
            dataSource.saveUser(oldUser)
            dataSource.saveUser(newUser)

            val result = useCase(Timestamp(200L))

            assertEquals(1, result.size)
            assertEquals(newUser, result[0])
        }
}
