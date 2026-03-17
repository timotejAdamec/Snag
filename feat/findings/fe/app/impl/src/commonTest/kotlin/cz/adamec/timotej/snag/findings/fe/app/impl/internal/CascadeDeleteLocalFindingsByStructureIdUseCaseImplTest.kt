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
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeDeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeDeleteLocalFindingsByStructureIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: CascadeDeleteLocalFindingsByStructureIdUseCase by inject()

    private val structureId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private fun createFinding(
        id: Uuid,
        structureId: Uuid,
    ) = FrontendFinding(
        finding =
            Finding(
                id = id,
                structureId = structureId,
                name = "Finding",
                description = null,
                type = FindingType.Classic(),
                coordinates = emptySet(),
                updatedAt = Timestamp(1L),
            ),
    )

    @Test
    fun `deletes all findings for the given structure`() =
        runTest(testDispatcher) {
            val finding1 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    structureId = structureId1,
                )
            val finding2 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    structureId = structureId1,
                )
            fakeFindingsDb.setFindings(listOf(finding1, finding2))

            useCase(structureId1)

            val result = fakeFindingsDb.getFindingsFlow(structureId1).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendFinding>>>(result)
            assertTrue(result.data.isEmpty())
        }

    @Test
    fun `does not delete findings from other structures`() =
        runTest(testDispatcher) {
            val findingForStructure1 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    structureId = structureId1,
                )
            val findingForStructure2 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    structureId = structureId2,
                )
            fakeFindingsDb.setFindings(listOf(findingForStructure1, findingForStructure2))

            useCase(structureId1)

            val result = fakeFindingsDb.getFindingsFlow(structureId2).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendFinding>>>(result)
            assertTrue(result.data.size == 1)
        }
}
