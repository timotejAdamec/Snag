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

import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveNewFindingUseCaseImplTest {
    private val findingsDb = FakeFindingsDb()
    private val findingsSync = FakeFindingsSync()
    private val useCase = SaveNewFindingUseCaseImpl(findingsDb, findingsSync, UuidProvider)

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `saves finding and enqueues sync`() = runTest {
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Crack in wall",
            description = "A large crack",
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        assertEquals(1, findingsSync.savedFindingIds.size)
        assertEquals(result.data, findingsSync.savedFindingIds.first())
    }

    @Test
    fun `uses empty coordinates by default`() = runTest {
        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Crack in wall",
            description = null,
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
        val savedFinding = getSavedFinding(result.data)
        assertEquals(emptyList(), savedFinding.coordinates)
    }

    @Test
    fun `uses provided coordinates`() = runTest {
        val coordinates = listOf(Coordinate(1.0f, 2.0f), Coordinate(3.0f, 4.0f))
        val request = SaveNewFindingRequest(
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
    fun `returns error when db save fails`() = runTest {
        findingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Name",
            description = null,
        )

        val result = useCase(request)

        assertIs<OfflineFirstDataResult.ProgrammerError>(result)
    }

    @Test
    fun `does not enqueue sync when save fails`() = runTest {
        findingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

        val request = SaveNewFindingRequest(
            structureId = structureId,
            name = "Name",
            description = null,
        )

        useCase(request)

        assertTrue(findingsSync.savedFindingIds.isEmpty())
    }

    @Test
    fun `saved finding has correct name and description`() = runTest {
        val request = SaveNewFindingRequest(
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

    private suspend fun getSavedFinding(id: Uuid): Finding {
        findingsDb.forcedFailure = null
        val result = findingsDb.getFindingFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
