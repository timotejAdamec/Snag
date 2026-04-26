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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.clients.fe.app.api.CanDeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.CanManageClientsUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.api.error.UiError
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.get
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDetailsEditViewModelTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeUsersDb: FakeUsersDb by inject()

    private val getClientUseCase: GetClientUseCase by inject()
    private val saveClientUseCase: SaveClientUseCase by inject()
    private val deleteClientUseCase: DeleteClientUseCase by inject()
    private val canDeleteClientUseCase: CanDeleteClientUseCase by inject()
    private val canManageClientsUseCase: CanManageClientsUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")

    private fun seedCurrentUser() {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                authProviderId = "mock-auth-provider-id",
                email = "user@example.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun createViewModel(clientId: Uuid? = null) =
        ClientDetailsEditViewModel(
            clientId = clientId,
            getClientUseCase = getClientUseCase,
            saveClientUseCase = saveClientUseCase,
            deleteClientUseCase = deleteClientUseCase,
            canDeleteClientUseCase = canDeleteClientUseCase,
            canManageClientsUseCase = canManageClientsUseCase,
            emailFormatRule = get(),
            phoneNumberRule = get(),
        )

    @Test
    fun `initial state is empty when clientId is null`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel(clientId = null)

            assertEquals("", viewModel.state.value.clientName)
            assertEquals("", viewModel.state.value.clientAddress)
            assertEquals("", viewModel.state.value.clientPhoneNumber)
            assertEquals("", viewModel.state.value.clientEmail)
        }

    @Test
    fun `loading client data updates state when clientId is provided`() =
        runTest(testDispatcher) {
            val clientId = Uuid.random()
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)
            seedCurrentUser()

            val viewModel = createViewModel(clientId = clientId)

            advanceUntilIdle()

            assertEquals("Test Client", viewModel.state.value.clientName)
            assertEquals("Test Address", viewModel.state.value.clientAddress)
            assertEquals("+420123456789", viewModel.state.value.clientPhoneNumber)
            assertEquals("test@example.com", viewModel.state.value.clientEmail)
        }

    @Test
    fun `onClientNameChange updates state`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()

            viewModel.onClientNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.clientName)
        }

    @Test
    fun `onClientAddressChange updates state`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()

            viewModel.onClientAddressChange("New Address")

            assertEquals("New Address", viewModel.state.value.clientAddress)
        }

    @Test
    fun `onClientPhoneNumberChange updates state`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()

            viewModel.onClientPhoneNumberChange("+420999888777")

            assertEquals("+420999888777", viewModel.state.value.clientPhoneNumber)
        }

    @Test
    fun `onClientEmailChange updates state`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()

            viewModel.onClientEmailChange("new@example.com")

            assertEquals("new@example.com", viewModel.state.value.clientEmail)
        }

    @Test
    fun `onSaveClient with empty name shows inline error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onSaveClient()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.clientNameError)
            subscriber.cancel()
        }

    @Test
    fun `onSaveClient with invalid email shows inline error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onClientNameChange("Name")
            viewModel.onClientEmailChange("invalid")

            viewModel.onSaveClient()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.clientEmailError)
            assertNull(viewModel.state.value.clientNameError)
            subscriber.cancel()
        }

    @Test
    fun `onSaveClient with invalid phone shows inline error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onClientNameChange("Name")
            viewModel.onClientPhoneNumberChange("abc")

            viewModel.onSaveClient()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.clientPhoneNumberError)
            assertNull(viewModel.state.value.clientNameError)
            subscriber.cancel()
        }

    @Test
    fun `onSaveClient with valid optional fields passes validation`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onClientNameChange("Name")
            viewModel.onClientEmailChange("test@example.com")
            viewModel.onClientPhoneNumberChange("+420123456789")

            viewModel.onSaveClient()

            val savedId = viewModel.saveEventFlow.first()
            assertNotNull(savedId)
            assertNull(viewModel.state.value.clientNameError)
            assertNull(viewModel.state.value.clientEmailError)
            assertNull(viewModel.state.value.clientPhoneNumberError)
            subscriber.cancel()
        }

    @Test
    fun `editing field clears its error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onSaveClient()
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.clientNameError)

            viewModel.onClientNameChange("N")

            assertNull(viewModel.state.value.clientNameError)
            subscriber.cancel()
        }

    @Test
    fun `onSaveClient successful sends save event`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onClientNameChange("Name")

            viewModel.onSaveClient()

            val savedId = viewModel.saveEventFlow.first()

            val savedClientResult = fakeClientsDb.getClientFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<AppClient?>>(savedClientResult)
            val savedClient = savedClientResult.data
            assertEquals("Name", savedClient?.name)
            subscriber.cancel()
        }

    @Test
    fun `onSaveClient failure sends error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            viewModel.onClientNameChange("Name")

            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveClient()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            subscriber.cancel()
        }

    @Test
    fun `onDelete success sends deleted event`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val clientId = Uuid.random()
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel(clientId = clientId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onDelete()

            viewModel.deletedSuccessfullyEventFlow.first()
            subscriber.cancel()
        }

    @Test
    fun `onDelete failure sends error`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val clientId = Uuid.random()
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel(clientId = clientId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            fakeClientsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onDelete()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            subscriber.cancel()
        }

    @Test
    fun `canDelete is true when client is not referenced`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val clientId = Uuid.random()
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel(clientId = clientId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDelete)
            subscriber.cancel()
        }

    @Test
    fun `canDelete is false when client is referenced by project`() =
        runTest(testDispatcher) {
            seedCurrentUser()
            val clientId = Uuid.random()
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = null,
                    phoneNumber = null,
                    email = null,
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)
            fakeProjectsDb.setProject(
                AppProjectData(
                    id = Uuid.random(),
                    name = "Test Project",
                    address = "Test Address",
                    clientId = clientId,
                    creatorId = Uuid.random(),
                    updatedAt = Timestamp(100L),
                ),
            )

            val viewModel = createViewModel(clientId = clientId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            assertFalse(viewModel.state.value.canDelete)
            subscriber.cancel()
        }
}
