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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteStructureUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeStructuresSync: FakeStructuresSync by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: DeleteStructureUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
            },
        )

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createStructure(id: Uuid, projectId: Uuid) = FrontendStructure(
        structure = Structure(
            id = id,
            projectId = projectId,
            name = "Structure",
            floorPlanUrl = null,
            updatedAt = Timestamp(1L),
        ),
    )

    private fun createFinding(id: Uuid, structureId: Uuid) = FrontendFinding(
        finding = Finding(
            id = id,
            structureId = structureId,
            name = "Finding",
            description = null,
            type = FindingType.Classic(),
            coordinates = emptyList(),
            updatedAt = Timestamp(1L),
        ),
    )

    @Test
    fun `deletes structure and cascade deletes findings`() = runTest(testDispatcher) {
        val structure = createStructure(id = structureId, projectId = projectId)
        fakeStructuresDb.setStructure(structure)

        val finding = createFinding(id = findingId, structureId = structureId)
        fakeFindingsDb.setFindings(listOf(finding))

        useCase(structureId)

        val structureResult = fakeStructuresDb.getStructureFlow(structureId).first()
        assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(structureResult)
        assertNull(structureResult.data)

        val findingsResult = fakeFindingsDb.getFindingsFlow(structureId).first()
        assertIs<OfflineFirstDataResult.Success<List<FrontendFinding>>>(findingsResult)
        assertTrue(findingsResult.data.isEmpty())
    }

    @Test
    fun `enqueues sync delete on success`() = runTest(testDispatcher) {
        val structure = createStructure(id = structureId, projectId = projectId)
        fakeStructuresDb.setStructure(structure)

        useCase(structureId)

        assertEquals(listOf(structureId), fakeStructuresSync.deletedStructureIds)
    }

    @Test
    fun `does not enqueue sync delete on failure`() = runTest(testDispatcher) {
        fakeStructuresDb.forcedFailure =
            OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

        useCase(structureId)

        assertTrue(fakeStructuresSync.deletedStructureIds.isEmpty())
    }
}
