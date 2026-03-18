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

package cz.adamec.timotej.snag.clients.be.app.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetClientUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ClientsDb by inject()
    private val useCase: GetClientUseCase by inject()

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val client =
        BackendClientData(
            id = clientId,
            name = "Test Client",
            address = "Test Address",
            phoneNumber = "+420123456789",
            email = "test@example.com",
            updatedAt = Timestamp(10L),
        )

    @Test
    fun `returns client when it exists`() =
        runTest(testDispatcher) {
            dataSource.saveClient(client)

            val result = useCase(clientId)

            assertEquals(client, result)
        }

    @Test
    fun `returns null when not found`() =
        runTest(testDispatcher) {
            val result = useCase(clientId)

            assertNull(result)
        }
}
