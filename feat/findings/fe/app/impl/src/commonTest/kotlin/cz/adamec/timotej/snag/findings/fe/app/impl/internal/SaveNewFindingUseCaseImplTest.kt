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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveNewFindingUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingsDb: FakeFindingsDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: SaveNewFindingUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `saves finding and enqueues sync`() =
        runTest(testDispatcher) {
            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Crack in wall",
                    description = "A large crack",
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(FINDING_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(result.data, pending[0].entityId)
            assertEquals(SyncOperationType.UPSERT, pending[0].operationType)
        }

    @Test
    fun `uses empty coordinates by default`() =
        runTest(testDispatcher) {
            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedFinding = getSavedFinding(result.data)
            assertEquals(emptySet(), savedFinding.coordinates)
        }

    @Test
    fun `uses provided coordinates`() =
        runTest(testDispatcher) {
            val coordinates = setOf(RelativeCoordinate(0.1f, 0.2f), RelativeCoordinate(0.3f, 0.4f))
            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                    coordinates = coordinates,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedFinding = getSavedFinding(result.data)
            assertEquals(coordinates, savedFinding.coordinates)
        }

    @Test
    fun `returns error when db save fails`() =
        runTest(testDispatcher) {
            fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Name",
                    description = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.ProgrammerError>(result)
        }

    @Test
    fun `does not enqueue sync when save fails`() =
        runTest(testDispatcher) {
            fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Name",
                    description = null,
                )

            useCase(request)

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }

    @Test
    fun `saved finding has correct name and description`() =
        runTest(testDispatcher) {
            val request =
                SaveNewFindingRequest(
                    structureId = structureId,
                    name = "Finding name",
                    description = "Finding description",
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedFinding = getSavedFinding(result.data)
            assertEquals("Finding name", savedFinding.name)
            assertEquals("Finding description", savedFinding.description)
            assertEquals(structureId, savedFinding.structureId)
        }

    private suspend fun getSavedFinding(id: Uuid): AppFinding {
        fakeFindingsDb.forcedFailure = null
        val result = fakeFindingsDb.getFindingFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
