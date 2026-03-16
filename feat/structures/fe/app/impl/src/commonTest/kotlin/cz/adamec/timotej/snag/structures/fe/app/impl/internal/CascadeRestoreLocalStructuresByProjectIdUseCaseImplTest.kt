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

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeRestoreLocalStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresApi
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeRestoreLocalStructuresByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeStructuresApi: FakeStructuresApi by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeFindingsApi: FakeFindingsApi by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: CascadeRestoreLocalStructuresByProjectIdUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structureId2 = Uuid.parse("00000000-0000-0000-0001-000000000002")

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
    fun `restores structures from API to local DB`() =
        runTest(testDispatcher) {
            val structure1 = createStructure(id = structureId1, projectId = projectId)
            val structure2 = createStructure(id = structureId2, projectId = projectId)
            fakeStructuresApi.setStructures(listOf(structure1, structure2))

            useCase(projectId)

            val result = fakeStructuresDb.getStructuresFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppStructure>>>(result)
            assertTrue(result.data.size == 2)
        }

    @Test
    fun `restores findings for each restored structure`() =
        runTest(testDispatcher) {
            val structure1 = createStructure(id = structureId1, projectId = projectId)
            fakeStructuresApi.setStructures(listOf(structure1))

            val finding1 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                    structureId = structureId1,
                )
            val finding2 =
                createFinding(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000002"),
                    structureId = structureId1,
                )
            fakeFindingsApi.setFindings(listOf(finding1, finding2))

            useCase(projectId)

            val findingsResult = fakeFindingsDb.getFindingsFlow(structureId1).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFinding>>>(findingsResult)
            assertTrue(findingsResult.data.size == 2)
        }

    @Test
    fun `does not crash when API fails`() =
        runTest(testDispatcher) {
            fakeStructuresApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(projectId)

            val result = fakeStructuresDb.getStructuresFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppStructure>>>(result)
            assertTrue(result.data.isEmpty())
        }
}
