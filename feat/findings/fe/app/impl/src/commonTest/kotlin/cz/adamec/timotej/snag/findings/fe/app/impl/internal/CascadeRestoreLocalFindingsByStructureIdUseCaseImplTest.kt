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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeRestoreLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeRestoreLocalFindingsByStructureIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingsApi: FakeFindingsApi by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: CascadeRestoreLocalFindingsByStructureIdUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")

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
    fun `restores findings from API to local DB`() =
        runTest(testDispatcher) {
            val finding1 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    structureId = structureId,
                )
            val finding2 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    structureId = structureId,
                )
            fakeFindingsApi.setFindings(listOf(finding1, finding2))

            useCase(structureId)

            val result = fakeFindingsDb.getFindingsFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFinding>>>(result)
            assertTrue(result.data.size == 2)
        }

    @Test
    fun `does not crash when API fails`() =
        runTest(testDispatcher) {
            fakeFindingsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(structureId)

            val result = fakeFindingsDb.getFindingsFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFinding>>>(result)
            assertTrue(result.data.isEmpty())
        }
}
