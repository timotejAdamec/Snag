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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.sync.STRUCTURE_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
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

class DeleteStructureUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: DeleteStructureUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createStructure(
        id: Uuid,
        projectId: Uuid,
    ) = AppStructureData(
        id = id,
        projectId = projectId,
        name = "Structure",
        floorPlanUrl = null,
        updatedAt = Timestamp(1L),
    )

    private fun createFinding(
        id: Uuid,
        structureId: Uuid,
    ) = AppFindingData(
        id = id,
        structureId = structureId,
        name = "Finding",
        description = null,
        type = FindingType.Classic(),
        coordinates = emptySet(),
        updatedAt = Timestamp(1L),
    )

    @Test
    fun `deletes structure and cascade deletes findings`() =
        runTest(testDispatcher) {
            val structure = createStructure(id = structureId, projectId = projectId)
            fakeStructuresDb.setStructure(structure)

            val finding = createFinding(id = findingId, structureId = structureId)
            fakeFindingsDb.setFindings(listOf(finding))

            useCase(structureId)

            val structureResult = fakeStructuresDb.getStructureFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<AppStructure?>>(structureResult)
            assertNull(structureResult.data)

            val findingsResult = fakeFindingsDb.getFindingsFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFinding>>>(findingsResult)
            assertTrue(findingsResult.data.isEmpty())
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val structure = createStructure(id = structureId, projectId = projectId)
            fakeStructuresDb.setStructure(structure)

            useCase(structureId)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(STRUCTURE_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(structureId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeStructuresDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(structureId)

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }
}
