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

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsSync
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
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

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::FakeClientsSync) bind ClientsSync::class
                singleOf(::FakeClientsApi) bind ClientsApi::class
                singleOf(::FakeClientsPullSyncCoordinator) bind ClientsPullSyncCoordinator::class
                singleOf(::FakeClientsPullSyncTimestampDataSource) bind ClientsPullSyncTimestampDataSource::class
            },
        )

    private fun createViewModel() =
        ClientsViewModel(
            getClientsUseCase = getClientsUseCase,
        )

    @Test
    fun `initial state has empty clients list`() =
        runTest {
            val viewModel = createViewModel()

            val clients = viewModel.state.value.clients
            assertTrue(clients.isEmpty())
        }

    @Test
    fun `loads clients from db`() =
        runTest {
            val client =
                FrontendClient(
                    client =
                        Client(
                            id = Uuid.random(),
                            name = "Test Client",
                            address = "Test Address",
                            phoneNumber = "+420123456789",
                            email = "test@example.com",
                            updatedAt = Timestamp(10L),
                        ),
                )
            fakeClientsDb.setClient(client)

            val viewModel = createViewModel()

            advanceUntilIdle()

            val clients = viewModel.state.value.clients
            assertEquals(1, clients.size)
            assertEquals("Test Client", clients.first().client.name)
        }
}
