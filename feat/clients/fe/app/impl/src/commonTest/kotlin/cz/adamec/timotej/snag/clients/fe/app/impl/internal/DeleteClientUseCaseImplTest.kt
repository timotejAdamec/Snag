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

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsSync
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteClientUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val fakeClientsSync: FakeClientsSync by inject()

    private val useCase: DeleteClientUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::FakeClientsSync) bind ClientsSync::class
            },
        )

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun createClient(id: Uuid) =
        FrontendClient(
            client =
                Client(
                    id = id,
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(100L),
                ),
        )

    @Test
    fun `deletes client from db`() =
        runTest(testDispatcher) {
            val client = createClient(clientId)
            fakeClientsDb.setClient(client)

            useCase(clientId)

            val result = fakeClientsDb.getClientFlow(clientId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendClient?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val client = createClient(clientId)
            fakeClientsDb.setClient(client)

            useCase(clientId)

            assertEquals(listOf(clientId), fakeClientsSync.deletedClientIds)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(clientId)

            assertTrue(fakeClientsSync.deletedClientIds.isEmpty())
        }
}
