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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync.INSPECTION_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
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

class DeleteInspectionUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: DeleteInspectionUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val inspectionId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    private fun createInspection(id: Uuid) =
        AppInspectionData(
            id = id,
            projectId = projectId,
            dateFrom = Timestamp(100L),
            dateTo = null,
            participants = "John Doe",
            climate = "Sunny",
            note = null,
            updatedAt = Timestamp(100L),
        )

    @Test
    fun `deletes inspection from db`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsDb.setInspection(inspection)

            useCase(inspectionId)

            val result = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<AppInspection?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsDb.setInspection(inspection)

            useCase(inspectionId)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(INSPECTION_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(inspectionId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(inspectionId)

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }
}
