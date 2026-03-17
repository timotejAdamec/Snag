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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.sync.CLIENT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteClientUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: DeleteClientUseCase by inject()

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun createClient(id: Uuid) =
        AppClientData(
            id = id,
            name = "Test Client",
            address = "Test Address",
            phoneNumber = "+420123456789",
            email = "test@example.com",
            updatedAt = Timestamp(100L),
        )

    @Test
    fun `deletes client from db`() =
        runTest(testDispatcher) {
            val client = createClient(clientId)
            fakeClientsDb.setClient(client)

            useCase(clientId)

            val result = fakeClientsDb.getClientFlow(clientId).first()
            assertIs<OfflineFirstDataResult.Success<AppClient?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val client = createClient(clientId)
            fakeClientsDb.setClient(client)

            useCase(clientId)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(CLIENT_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(clientId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(clientId)

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }
}
