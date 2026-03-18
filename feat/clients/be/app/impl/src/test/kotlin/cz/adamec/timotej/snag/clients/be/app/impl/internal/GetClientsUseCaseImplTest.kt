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

import cz.adamec.timotej.snag.clients.be.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetClientsUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ClientsDb by inject()
    private val useCase: GetClientsUseCase by inject()

    @Test
    fun `returns empty list when none exist`() =
        runTest(testDispatcher) {
            val result = useCase()

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns all clients`() =
        runTest(testDispatcher) {
            val client1 =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Client 1",
                    address = "Address 1",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            val client2 =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                    name = "Client 2",
                    address = "Address 2",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            dataSource.saveClient(client1)
            dataSource.saveClient(client2)

            val result = useCase()

            assertEquals(listOf(client1, client2), result)
        }
}
