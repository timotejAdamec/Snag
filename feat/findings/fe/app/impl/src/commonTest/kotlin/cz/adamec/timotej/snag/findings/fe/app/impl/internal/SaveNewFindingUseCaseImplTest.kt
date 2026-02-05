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

import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
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
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveNewFindingUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeFindingsDb: FakeFindingsDb by inject()
    private val fakeFindingsSync: FakeFindingsSync by inject()

    private val useCase: SaveNewFindingUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsSync) bind FindingsSync::class
            },
        )

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `saves finding and enqueues sync`() = runTest(testDispatcher) {
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Crack in wall",
            description = "A large crack",
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        assertEquals(1, fakeFindingsSync.savedFindingIds.size)
        assertEquals(result.data, fakeFindingsSync.savedFindingIds.first())
    }

    @Test
    fun `uses empty coordinates by default`() = runTest(testDispatcher) {
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Crack in wall",
            description = null,
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        val savedFinding = getSavedFinding(result.data)
        assertEquals(emptyList(), savedFinding.finding.coordinates)
    }

    @Test
    fun `uses provided coordinates`() = runTest(testDispatcher) {
        val coordinates = listOf(RelativeCoordinate(0.1f, 0.2f), RelativeCoordinate(0.3f, 0.4f))
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Crack in wall",
            description = null,
            coordinates = coordinates,
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        val savedFinding = getSavedFinding(result.data)
        assertEquals(coordinates, savedFinding.finding.coordinates)
    }

    @Test
    fun `returns error when db save fails`() = runTest(testDispatcher) {
        fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Name",
            description = null,
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.ProgrammerError>(result)
    }

    @Test
    fun `does not enqueue sync when save fails`() = runTest(testDispatcher) {
        fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Name",
            description = null,
        )

        useCase(request)

        assertTrue(fakeFindingsSync.savedFindingIds.isEmpty())
    }

    @Test
    fun `saved finding has correct name and description`() = runTest(testDispatcher) {
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Finding name",
            description = "Finding description",
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        val savedFinding = getSavedFinding(result.data)
        assertEquals("Finding name", savedFinding.finding.name)
        assertEquals("Finding description", savedFinding.finding.description)
        assertEquals(structureId, savedFinding.finding.structureId)
    }

    private suspend fun getSavedFinding(id: Uuid): FrontendFinding {
        fakeFindingsDb.forcedFailure = null
        val result = fakeFindingsDb.getFindingFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
