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
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.PullStructureChangesUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresApi
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class PullStructureChangesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeStructuresApi: FakeStructuresApi by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakePullSyncTimestampDataSource: FakeStructuresPullSyncTimestampDataSource by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val useCase: PullStructureChangesUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresApi) bind StructuresApi::class
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
            },
        )

    private fun createStructure(id: Uuid) =
        FrontendStructure(
            structure =
                Structure(
                    id = id,
                    projectId = projectId,
                    name = "Test Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(100L),
                ),
        )

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
                coordinates = emptyList(),
                updatedAt = Timestamp(1L),
            ),
    )

    @Test
    fun `upserts alive structures to db`() =
        runTest(testDispatcher) {
            val structure = createStructure(structureId)
            fakeStructuresApi.modifiedSinceResults =
                listOf(
                    StructureSyncResult.Updated(structure = structure),
                )

            useCase(projectId)

            val result = fakeStructuresDb.getStructureFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(result)
            assertNotNull(result.data)
            assertEquals(structureId, result.data!!.structure.id)
        }

    @Test
    fun `deletes soft-deleted structures and cascades findings`() =
        runTest(testDispatcher) {
            val structure = createStructure(structureId)
            fakeStructuresDb.setStructure(structure)

            val finding = createFinding(id = findingId, structureId = structureId)
            fakeFindingsDb.setFinding(finding)

            fakeStructuresApi.modifiedSinceResults =
                listOf(
                    StructureSyncResult.Deleted(id = structureId),
                )

            useCase(projectId)

            val findingsResult = fakeFindingsDb.getFindingsFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendFinding>>>(findingsResult)
            assertTrue(findingsResult.data.isEmpty())

            val result = fakeStructuresDb.getStructureFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `stores last synced timestamp on success`() =
        runTest(testDispatcher) {
            fakeStructuresApi.modifiedSinceResults = emptyList()

            useCase(projectId)

            assertNotNull(fakePullSyncTimestampDataSource.getLastSyncedAt(projectId))
        }

    @Test
    fun `does not store timestamp on API failure`() =
        runTest(testDispatcher) {
            fakeStructuresApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(projectId)

            assertNull(fakePullSyncTimestampDataSource.getLastSyncedAt(projectId))
        }
}
