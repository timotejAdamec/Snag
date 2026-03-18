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

import cz.adamec.timotej.snag.clients.be.app.api.GetClientsModifiedSinceUseCase
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetClientsModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ClientsDb by inject()
    private val useCase: GetClientsModifiedSinceUseCase by inject()

    @Test
    fun `returns empty list when no clients exist`() =
        runTest(testDispatcher) {
            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns clients with updatedAt after since`() =
        runTest(testDispatcher) {
            val client =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Client 1",
                    address = "Address 1",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveClient(client)

            val result = useCase(since = Timestamp(100L))

            assertEquals(listOf(client), result)
        }

    @Test
    fun `excludes clients with updatedAt before since`() =
        runTest(testDispatcher) {
            val client =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Client 1",
                    address = "Address 1",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(50L),
                )
            dataSource.saveClient(client)

            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted clients when deletedAt is after since`() =
        runTest(testDispatcher) {
            val client =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Client 1",
                    address = "Address 1",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveClient(client)

            val result = useCase(since = Timestamp(100L))

            assertEquals(listOf(client), result)
        }

    @Test
    fun `excludes deleted clients when deletedAt is before since`() =
        runTest(testDispatcher) {
            val client =
                BackendClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Client 1",
                    address = "Address 1",
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(80L),
                )
            dataSource.saveClient(client)

            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
