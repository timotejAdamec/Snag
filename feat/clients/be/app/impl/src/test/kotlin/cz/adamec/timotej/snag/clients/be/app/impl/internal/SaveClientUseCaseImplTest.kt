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

import cz.adamec.timotej.snag.clients.be.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class SaveClientUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ClientsDb by inject()
    private val useCase: SaveClientUseCase by inject()

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
    fun `saves client to data source`() =
        runTest(testDispatcher) {
            useCase(client)

            val stored = dataSource.getClient(clientId)
            assertEquals(client, stored)
        }

    @Test
    fun `does not save client if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedClient =
                client.copy(
                    updatedAt = Timestamp(value = 20L),
                )
            dataSource.saveClient(savedClient)

            useCase(client)

            assertEquals(savedClient, dataSource.getClient(clientId))
        }

    @Test
    fun `returns null if client was not present`() =
        runTest(testDispatcher) {
            val result = useCase(client)

            assertNull(result)
        }

    @Test
    fun `returns saved client if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedClient =
                client.copy(
                    updatedAt = Timestamp(value = 20L),
                )
            dataSource.saveClient(savedClient)

            val result = useCase(client)

            assertEquals(savedClient, result)
        }

    @Test
    fun `returns null if saved updated at is earlier than the new one`() =
        runTest(testDispatcher) {
            dataSource.saveClient(client)

            val newerClient =
                client.copy(
                    name = "New name",
                    updatedAt = Timestamp(value = 20L),
                )

            val result = useCase(newerClient)

            assertNull(result)
        }

    @Test
    fun `restores soft-deleted client when saved with newer updatedAt`() =
        runTest(testDispatcher) {
            val deletedClient = client.copy(deletedAt = Timestamp(15L))
            dataSource.saveClient(deletedClient)

            val restoredClient =
                client.copy(
                    name = "Restored",
                    updatedAt = Timestamp(value = 20L),
                )

            val result = useCase(restoredClient)

            assertNull(result)
            val stored = dataSource.getClient(clientId)
            assertNotNull(stored)
            assertNull(stored.deletedAt)
            assertEquals("Restored", stored.name)
        }

    @Test
    fun `does not restore soft-deleted client when saved with older updatedAt`() =
        runTest(testDispatcher) {
            val deletedClient = client.copy(deletedAt = Timestamp(15L))
            dataSource.saveClient(deletedClient)

            val olderClient =
                client.copy(
                    updatedAt = Timestamp(value = 5L),
                )

            val result = useCase(olderClient)

            assertNotNull(result)
            assertEquals(deletedClient, result)
        }
}
