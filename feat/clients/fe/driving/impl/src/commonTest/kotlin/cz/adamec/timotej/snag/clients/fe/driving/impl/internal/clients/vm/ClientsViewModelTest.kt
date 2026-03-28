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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm

import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.clients.fe.app.api.CanManageClientsUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ClientsViewModelTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()

    private val getClientsUseCase: GetClientsUseCase by inject()
    private val canManageClientsUseCase: CanManageClientsUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                factory<CanManageClientsUseCase> {
                    object : CanManageClientsUseCase {
                        override fun invoke() = flowOf(true)
                    }
                }
            },
        )

    private fun createViewModel() =
        ClientsViewModel(
            getClientsUseCase = getClientsUseCase,
            canManageClientsUseCase = canManageClientsUseCase,
        )

    @Test
    fun `initial state has empty clients list`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            val clients = viewModel.state.value.clients
            assertTrue(clients.isEmpty())
        }

    @Test
    fun `loads clients from db`() =
        runTest(testDispatcher) {
            val client =
                AppClientData(
                    id = Uuid.random(),
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(10L),
                )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel()

            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            val clients = viewModel.state.value.clients
            assertEquals(1, clients.size)
            assertEquals("Test Client", clients.first().name)
            subscriber.cancel()
        }
}
