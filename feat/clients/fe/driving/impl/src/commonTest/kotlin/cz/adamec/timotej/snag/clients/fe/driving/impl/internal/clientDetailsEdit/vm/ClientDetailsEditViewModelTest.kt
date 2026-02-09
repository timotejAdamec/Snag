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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsSync
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDetailsEditViewModelTest : FrontendKoinInitializedTest() {

    private val fakeClientsDb: FakeClientsDb by inject()

    private val getClientUseCase: GetClientUseCase by inject()
    private val saveClientUseCase: SaveClientUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeClientsApi) bind ClientsApi::class
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::FakeClientsSync) bind ClientsSync::class
            },
        )

    private fun createViewModel(clientId: Uuid? = null) =
        ClientDetailsEditViewModel(
            clientId = clientId,
            getClientUseCase = getClientUseCase,
            saveClientUseCase = saveClientUseCase,
        )

    @Test
    fun `initial state is empty when clientId is null`() =
        runTest {
            val viewModel = createViewModel(clientId = null)

            assertEquals("", viewModel.state.value.clientName)
            assertEquals("", viewModel.state.value.clientAddress)
            assertEquals("", viewModel.state.value.clientPhoneNumber)
            assertEquals("", viewModel.state.value.clientEmail)
        }

    @Test
    fun `loading client data updates state when clientId is provided`() =
        runTest {
            val clientId = Uuid.random()
            val client = FrontendClient(
                client = Client(
                    id = clientId,
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(10L),
                ),
            )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel(clientId = clientId)

            advanceUntilIdle()

            assertEquals("Test Client", viewModel.state.value.clientName)
            assertEquals("Test Address", viewModel.state.value.clientAddress)
            assertEquals("+420123456789", viewModel.state.value.clientPhoneNumber)
            assertEquals("test@example.com", viewModel.state.value.clientEmail)
        }

    @Test
    fun `onClientNameChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onClientNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.clientName)
        }

    @Test
    fun `onClientAddressChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onClientAddressChange("New Address")

            assertEquals("New Address", viewModel.state.value.clientAddress)
        }

    @Test
    fun `onClientPhoneNumberChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onClientPhoneNumberChange("+420999888777")

            assertEquals("+420999888777", viewModel.state.value.clientPhoneNumber)
        }

    @Test
    fun `onClientEmailChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onClientEmailChange("new@example.com")

            assertEquals("new@example.com", viewModel.state.value.clientEmail)
        }

    @Test
    fun `onSaveClient with empty name sends error`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onSaveClient()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.CustomUserMessage>(error)
            assertEquals("Client name cannot be empty", error.message)
        }

    @Test
    fun `onSaveClient successful sends save event`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onClientNameChange("Name")

            viewModel.onSaveClient()

            val savedId = viewModel.saveEventFlow.first()

            val savedClientResult = fakeClientsDb.getClientFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendClient?>>(savedClientResult)
            val savedClient = savedClientResult.data
            assertEquals("Name", savedClient?.client?.name)
        }

    @Test
    fun `onSaveClient failure sends error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onClientNameChange("Name")

            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveClient()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }
}
