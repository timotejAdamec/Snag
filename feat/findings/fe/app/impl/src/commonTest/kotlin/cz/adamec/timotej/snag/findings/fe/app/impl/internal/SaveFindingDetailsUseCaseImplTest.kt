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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingDetailsRequest
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveFindingDetailsUseCaseImplTest {
    private val findingsDb = FakeFindingsDb()
    private val findingsSync = FakeFindingsSync()
    private val useCase = SaveFindingDetailsUseCaseImpl(findingsDb, findingsSync)

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    private val existingFinding = Finding(
        id = findingId,
        structureId = structureId,
        name = "Old name",
        description = "Old description",
        coordinates = emptyList(),
    )

    @Test
    fun `updates name and description`() = runTest {
        findingsDb.setFinding(existingFinding)

        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        val result = useCase(request)

        assertIs<OfflineFirstUpdateDataResult.Success>(result)
    }

    @Test
    fun `enqueues sync on success`() = runTest {
        findingsDb.setFinding(existingFinding)

        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        useCase(request)

        assertTrue(findingsSync.savedFindingIds.contains(findingId))
    }

    @Test
    fun `returns NotFound when finding does not exist`() = runTest {
        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        val result = useCase(request)

        assertIs<OfflineFirstUpdateDataResult.NotFound>(result)
    }

    @Test
    fun `does not enqueue sync when not found`() = runTest {
        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        useCase(request)

        assertTrue(findingsSync.savedFindingIds.isEmpty())
    }

    @Test
    fun `returns error when db fails`() = runTest {
        findingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("DB error"))

        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        val result = useCase(request)

        assertIs<OfflineFirstUpdateDataResult.ProgrammerError>(result)
    }

    @Test
    fun `does not enqueue sync when update fails`() = runTest {
        findingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("DB error"))

        val request = SaveFindingDetailsRequest(
            findingId = findingId,
            name = "New name",
            description = "New description",
        )

        useCase(request)

        assertTrue(findingsSync.savedFindingIds.isEmpty())
    }
}
