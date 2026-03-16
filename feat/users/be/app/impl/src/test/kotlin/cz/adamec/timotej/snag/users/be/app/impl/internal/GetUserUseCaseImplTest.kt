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
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.model.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetUserUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: UsersDb by inject()
    private val useCase: GetUserUseCase by inject()

    private val userId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `returns user when found`() =
        runTest(testDispatcher) {
            val user =
                BackendUserData(
                    id = userId,
                    entraId = "entra-1",
                    email = "user@example.com",
                    role = UserRole.PASSPORT_LEAD,
                    updatedAt = Timestamp(100L),
                )
            dataSource.saveUser(user)

            val result = useCase(userId)

            assertEquals(user, result)
        }

    @Test
    fun `returns null when not found`() =
        runTest(testDispatcher) {
            val result = useCase(userId)

            assertNull(result)
        }
}
