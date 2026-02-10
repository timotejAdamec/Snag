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

import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.model.SaveClientRequest
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsSync
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveClientUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val fakeClientsSync: FakeClientsSync by inject()

    private val useCase: SaveClientUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::FakeClientsSync) bind ClientsSync::class
            },
        )

    @Test
    fun `saves client and enqueues sync`() =
        runTest(testDispatcher) {
            val request =
                SaveClientRequest(
                    id = null,
                    name = "Test Client",
                    address = "123 Main St",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertEquals(1, fakeClientsSync.savedClientIds.size)
            assertEquals(result.data, fakeClientsSync.savedClientIds.first())
        }

    @Test
    fun `saved client has correct fields`() =
        runTest(testDispatcher) {
            val request =
                SaveClientRequest(
                    id = null,
                    name = "Client Name",
                    address = "Client Address",
                    phoneNumber = "+420111222333",
                    email = "client@example.com",
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedClient = getSavedClient(result.data)
            assertEquals("Client Name", savedClient.client.name)
            assertEquals("Client Address", savedClient.client.address)
            assertEquals("+420111222333", savedClient.client.phoneNumber)
            assertEquals("client@example.com", savedClient.client.email)
        }

    @Test
    fun `uses provided id when present`() =
        runTest(testDispatcher) {
            val id = Uuid.parse("00000000-0000-0000-0000-000000000001")
            val request =
                SaveClientRequest(
                    id = id,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertEquals(id, result.data)
        }

    @Test
    fun `generates new id when id is null`() =
        runTest(testDispatcher) {
            val request =
                SaveClientRequest(
                    id = null,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertNotNull(result.data)
        }

    @Test
    fun `returns error when db save fails`() =
        runTest(testDispatcher) {
            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveClientRequest(
                    id = null,
                    name = "Name",
                    address = null,
                    phoneNumber = null,
                    email = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.ProgrammerError>(result)
        }

    @Test
    fun `does not enqueue sync when save fails`() =
        runTest(testDispatcher) {
            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveClientRequest(
                    id = null,
                    name = "Name",
                    address = null,
                    phoneNumber = null,
                    email = null,
                )

            useCase(request)

            assertTrue(fakeClientsSync.savedClientIds.isEmpty())
        }

    private suspend fun getSavedClient(id: Uuid): FrontendClient {
        fakeClientsDb.forcedFailure = null
        val result = fakeClientsDb.getClientFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
