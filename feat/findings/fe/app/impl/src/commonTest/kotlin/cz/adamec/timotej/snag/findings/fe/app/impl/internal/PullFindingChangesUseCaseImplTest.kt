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
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.findings.fe.app.api.PullFindingChangesUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsPullSyncCoordinator
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncCoordinator
import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
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
import kotlin.uuid.Uuid

class PullFindingChangesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingsApi: FakeFindingsApi by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()
    private val fakePullSyncTimestampDataSource: FakeFindingsPullSyncTimestampDataSource by inject()

    private val useCase: PullFindingChangesUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsApi) bind FindingsApi::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsSync) bind FindingsSync::class
                singleOf(::FakeFindingsPullSyncCoordinator) bind FindingsPullSyncCoordinator::class
                singleOf(::FakeFindingsPullSyncTimestampDataSource) bind FindingsPullSyncTimestampDataSource::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    private fun createFinding(id: Uuid) =
        FrontendFinding(
            finding =
                Finding(
                    id = id,
                    structureId = structureId,
                    name = "Test Finding",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptyList(),
                    updatedAt = Timestamp(100L),
                ),
        )

    @Test
    fun `upserts alive findings to db`() =
        runTest(testDispatcher) {
            val finding = createFinding(findingId)
            fakeFindingsApi.modifiedSinceResults =
                listOf(
                    FindingSyncResult.Updated(finding = finding),
                )

            useCase(structureId)

            val result = fakeFindingsDb.getFindingFlow(findingId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendFinding?>>(result)
            assertNotNull(result.data)
            assertEquals(findingId, result.data!!.finding.id)
        }

    @Test
    fun `deletes soft-deleted findings from db`() =
        runTest(testDispatcher) {
            val finding = createFinding(findingId)
            fakeFindingsDb.setFinding(finding)

            fakeFindingsApi.modifiedSinceResults =
                listOf(
                    FindingSyncResult.Deleted(id = findingId),
                )

            useCase(structureId)

            val result = fakeFindingsDb.getFindingFlow(findingId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendFinding?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `stores last synced timestamp on success`() =
        runTest(testDispatcher) {
            fakeFindingsApi.modifiedSinceResults = emptyList()

            useCase(structureId)

            assertNotNull(fakePullSyncTimestampDataSource.getLastSyncedAt(structureId))
        }

    @Test
    fun `does not store timestamp on API failure`() =
        runTest(testDispatcher) {
            fakeFindingsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(structureId)

            assertNull(fakePullSyncTimestampDataSource.getLastSyncedAt(structureId))
        }
}
