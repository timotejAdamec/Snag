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
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingCoordinatesUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingCoordinatesRequest
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
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

class SaveFindingCoordinatesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingsDb: FakeFindingsDb by inject()
    private val fakeFindingsSync: FakeFindingsSync by inject()

    private val useCase: SaveFindingCoordinatesUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    private val existingFinding =
        FrontendFinding(
            finding =
                Finding(
                    id = findingId,
                    structureId = structureId,
                    name = "Finding",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = listOf(RelativeCoordinate(0.1f, 0.2f)),
                    updatedAt = Timestamp(10L),
                ),
        )

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsSync) bind FindingsSync::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    @Test
    fun `updates coordinates`() =
        runTest(testDispatcher) {
            fakeFindingsDb.setFinding(existingFinding)

            val newCoordinates = listOf(RelativeCoordinate(0.5f, 0.6f), RelativeCoordinate(0.7f, 0.8f))
            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = newCoordinates,
                )

            val result = useCase(request)

            assertIs<OfflineFirstUpdateDataResult.Success>(result)
        }

    @Test
    fun `enqueues sync on success`() =
        runTest(testDispatcher) {
            fakeFindingsDb.setFinding(existingFinding)

            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.6f)),
                )

            useCase(request)

            assertTrue(fakeFindingsSync.savedFindingIds.contains(findingId))
        }

    @Test
    fun `returns NotFound when finding does not exist`() =
        runTest(testDispatcher) {
            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.6f)),
                )

            val result = useCase(request)

            assertIs<OfflineFirstUpdateDataResult.NotFound>(result)
        }

    @Test
    fun `does not enqueue sync when not found`() =
        runTest(testDispatcher) {
            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.6f)),
                )

            useCase(request)

            assertTrue(fakeFindingsSync.savedFindingIds.isEmpty())
        }

    @Test
    fun `returns error when db fails`() =
        runTest(testDispatcher) {
            fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("DB error"))

            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.6f)),
                )

            val result = useCase(request)

            assertIs<OfflineFirstUpdateDataResult.ProgrammerError>(result)
        }

    @Test
    fun `does not enqueue sync when update fails`() =
        runTest(testDispatcher) {
            fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("DB error"))

            val request =
                SaveFindingCoordinatesRequest(
                    findingId = findingId,
                    coordinates = listOf(RelativeCoordinate(0.5f, 0.6f)),
                )

            useCase(request)

            assertTrue(fakeFindingsSync.savedFindingIds.isEmpty())
        }
}
