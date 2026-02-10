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

package cz.adamec.timotej.snag.clients.fe.driven.internal.sync

import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
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
import kotlin.uuid.Uuid

class ClientSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeClientsApi: FakeClientsApi by inject()
    private val fakeClientsDb: FakeClientsDb by inject()

    private val handler: ClientSyncHandler by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeClientsApi) bind ClientsApi::class
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::ClientSyncHandler)
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    @Test
    fun `upsert reads from db and calls api`() =
        runTest(testDispatcher) {
            val client =
                FrontendClient(
                    client = Client(Uuid.random(), "Test Client", "123 Street", "+420123456789", "test@example.com", Timestamp(10L)),
                )
            fakeClientsDb.setClient(client)

            val result = handler.execute(client.client.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `upsert saves fresher dto from api to db`() =
        runTest(testDispatcher) {
            val client =
                FrontendClient(
                    client = Client(Uuid.random(), "Original", "123 Street", null, null, Timestamp(10L)),
                )
            fakeClientsDb.setClient(client)

            val fresherClient = client.copy(client = client.client.copy(name = "Updated by API"))
            fakeClientsApi.saveClientResponseOverride = { OnlineDataResult.Success(fresherClient) }

            val result = handler.execute(client.client.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            val dbResult = fakeClientsDb.getClientFlow(client.client.id).first()
            val savedClient = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals("Updated by API", savedClient?.client?.name)
        }

    @Test
    fun `upsert when entity not in db returns entity not found`() =
        runTest(testDispatcher) {
            val result = handler.execute(Uuid.random(), SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `upsert when api fails returns failure`() =
        runTest(testDispatcher) {
            val client =
                FrontendClient(
                    client = Client(Uuid.random(), "Test Client", "123 Street", null, null, Timestamp(10L)),
                )
            fakeClientsDb.setClient(client)
            fakeClientsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(client.client.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Failure, result)
        }

    @Test
    fun `delete calls api and returns success`() =
        runTest(testDispatcher) {
            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `delete when api fails returns failure`() =
        runTest(testDispatcher) {
            fakeClientsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Failure, result)
        }
}
