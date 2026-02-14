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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.PullInspectionChangesUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionSyncResult
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
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

class PullInspectionChangesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsApi: FakeInspectionsApi by inject()
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakePullSyncTimestampDataSource: FakeInspectionsPullSyncTimestampDataSource by inject()

    private val useCase: PullInspectionChangesUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val inspectionId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsApi) bind InspectionsApi::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
            },
        )

    private fun createInspection(id: Uuid) =
        FrontendInspection(
            inspection =
                Inspection(
                    id = id,
                    projectId = projectId,
                    startedAt = Timestamp(100L),
                    endedAt = null,
                    participants = "John Doe",
                    climate = "Sunny",
                    note = null,
                    updatedAt = Timestamp(100L),
                ),
        )

    @Test
    fun `upserts alive inspections to db`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsApi.modifiedSinceResults =
                listOf(
                    InspectionSyncResult.Updated(inspection = inspection),
                )

            useCase(projectId)

            val result = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendInspection?>>(result)
            assertNotNull(result.data)
            assertEquals(inspectionId, result.data!!.inspection.id)
        }

    @Test
    fun `deletes soft-deleted inspections`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsDb.setInspection(inspection)

            fakeInspectionsApi.modifiedSinceResults =
                listOf(
                    InspectionSyncResult.Deleted(id = inspectionId),
                )

            useCase(projectId)

            val result = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendInspection?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `stores last synced timestamp on success`() =
        runTest(testDispatcher) {
            fakeInspectionsApi.modifiedSinceResults = emptyList()

            useCase(projectId)

            assertNotNull(fakePullSyncTimestampDataSource.getLastSyncedAt(projectId))
        }

    @Test
    fun `does not store timestamp on API failure`() =
        runTest(testDispatcher) {
            fakeInspectionsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(projectId)

            assertNull(fakePullSyncTimestampDataSource.getLastSyncedAt(projectId))
        }
}
