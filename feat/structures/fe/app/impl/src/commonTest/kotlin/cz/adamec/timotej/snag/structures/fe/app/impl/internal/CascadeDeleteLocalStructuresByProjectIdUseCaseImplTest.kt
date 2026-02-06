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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.app.test.FakeDeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeDeleteLocalStructuresByProjectIdUseCase
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeDeleteLocalStructuresByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val useCase: CascadeDeleteLocalStructuresByProjectIdUseCase by inject()

    private val fakeDeleteFindings = FakeDeleteLocalFindingsByStructureIdUseCase()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                single { fakeDeleteFindings } bind DeleteLocalFindingsByStructureIdUseCase::class
            },
        )

    private val projectId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val projectId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private val structureId1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structureId2 = Uuid.parse("00000000-0000-0000-0001-000000000002")
    private val structureId3 = Uuid.parse("00000000-0000-0000-0001-000000000003")

    private fun createStructure(id: Uuid, projectId: Uuid) = FrontendStructure(
        structure = Structure(
            id = id,
            projectId = projectId,
            name = "Structure",
            floorPlanUrl = null,
            updatedAt = Timestamp(1L),
        ),
    )

    @Test
    fun `deletes all structures for the given project`() = runTest(testDispatcher) {
        val structure1 = createStructure(id = structureId1, projectId = projectId1)
        val structure2 = createStructure(id = structureId2, projectId = projectId1)
        fakeStructuresDb.setStructures(listOf(structure1, structure2))

        useCase(projectId1)

        val result = fakeStructuresDb.getStructuresFlow(projectId1).first()
        assertIs<OfflineFirstDataResult.Success<List<FrontendStructure>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `calls delete findings for each structure`() = runTest(testDispatcher) {
        val structure1 = createStructure(id = structureId1, projectId = projectId1)
        val structure2 = createStructure(id = structureId2, projectId = projectId1)
        fakeStructuresDb.setStructures(listOf(structure1, structure2))

        useCase(projectId1)

        assertTrue(fakeDeleteFindings.deletedStructureIds.contains(structureId1))
        assertTrue(fakeDeleteFindings.deletedStructureIds.contains(structureId2))
    }

    @Test
    fun `does not delete structures from other projects`() = runTest(testDispatcher) {
        val structureForProject1 = createStructure(id = structureId1, projectId = projectId1)
        val structureForProject2 = createStructure(id = structureId3, projectId = projectId2)
        fakeStructuresDb.setStructures(listOf(structureForProject1, structureForProject2))

        useCase(projectId1)

        val result = fakeStructuresDb.getStructuresFlow(projectId2).first()
        assertIs<OfflineFirstDataResult.Success<List<FrontendStructure>>>(result)
        assertTrue(result.data.size == 1)
    }
}
