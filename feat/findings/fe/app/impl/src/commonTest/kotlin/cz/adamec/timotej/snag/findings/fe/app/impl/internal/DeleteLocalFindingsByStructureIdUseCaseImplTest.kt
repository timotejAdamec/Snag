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
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsPullSyncCoordinator
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncCoordinator
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteLocalFindingsByStructureIdUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: DeleteLocalFindingsByStructureIdUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsSync) bind FindingsSync::class
                singleOf(::FakeFindingsPullSyncCoordinator) bind FindingsPullSyncCoordinator::class
                singleOf(::FakeFindingsPullSyncTimestampDataSource) bind FindingsPullSyncTimestampDataSource::class
            },
        )

    private val structureId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private fun createFinding(id: Uuid, structureId: Uuid) = FrontendFinding(
        finding = Finding(
            id = id,
            structureId = structureId,
            name = "Finding",
            description = null,
            coordinates = emptyList(),
            updatedAt = Timestamp(1L),
        ),
    )

    @Test
    fun `deletes all findings for the given structure`() = runTest(testDispatcher) {
        val finding1 = createFinding(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            structureId = structureId1,
        )
        val finding2 = createFinding(
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
    fun `does not delete findings from other structures`() = runTest(testDispatcher) {
        val findingForStructure1 = createFinding(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            structureId = structureId1,
        )
        val findingForStructure2 = createFinding(
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
