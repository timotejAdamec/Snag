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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.sync

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
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

class InspectionSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsApi: FakeInspectionsApi by inject()
    private val fakeInspectionsDb: FakeInspectionsDb by inject()

    private val handler: InspectionSyncHandler by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsApi) bind InspectionsApi::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::InspectionSyncHandler)
            },
        )

    private fun createInspection(id: Uuid) =
        FrontendInspection(
            inspection =
                Inspection(
                    id = id,
                    projectId = projectId,
                    startedAt = Timestamp(100L),
                    endedAt = null,
                    participants = "John Doe",
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(10L),
                ),
        )

    @Test
    fun `upsert reads from db and calls api`() =
        runTest(testDispatcher) {
            val inspection = createInspection(Uuid.random())
            fakeInspectionsDb.setInspection(inspection)

            val result = handler.execute(inspection.inspection.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `upsert saves fresher dto from api to db`() =
        runTest(testDispatcher) {
            val inspection = createInspection(Uuid.random())
            fakeInspectionsDb.setInspection(inspection)

            val fresherInspection =
                inspection.copy(
                    inspection = inspection.inspection.copy(participants = "Updated by API"),
                )
            fakeInspectionsApi.saveInspectionResponseOverride = { OnlineDataResult.Success(fresherInspection) }

            val result = handler.execute(inspection.inspection.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            val dbResult = fakeInspectionsDb.getInspectionFlow(inspection.inspection.id).first()
            val saved = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals("Updated by API", saved?.inspection?.participants)
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
            val inspection = createInspection(Uuid.random())
            fakeInspectionsDb.setInspection(inspection)
            fakeInspectionsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(inspection.inspection.id, SyncOperationType.UPSERT)

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
            fakeInspectionsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Failure, result)
        }
}
