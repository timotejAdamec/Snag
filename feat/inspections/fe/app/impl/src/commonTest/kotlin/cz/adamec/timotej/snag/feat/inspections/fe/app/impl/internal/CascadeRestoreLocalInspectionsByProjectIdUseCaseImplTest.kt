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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeRestoreLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeRestoreLocalInspectionsByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsApi: FakeInspectionsApi by inject()
    private val fakeInspectionsDb: FakeInspectionsDb by inject()

    private val useCase: CascadeRestoreLocalInspectionsByProjectIdUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun createInspection(
        id: Uuid,
        projectId: Uuid,
    ) = AppInspectionData(
        id = id,
        projectId = projectId,
        dateFrom = null,
        dateTo = null,
        participants = null,
        climate = null,
        note = null,
        updatedAt = Timestamp(1L),
    )

    @Test
    fun `restores inspections from API to local DB`() =
        runTest(testDispatcher) {
            val inspection1 =
                createInspection(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                )
            val inspection2 =
                createInspection(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    projectId = projectId,
                )
            fakeInspectionsApi.setInspections(listOf(inspection1, inspection2))

            useCase(projectId)

            val result = fakeInspectionsDb.getInspectionsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppInspection>>>(result)
            assertTrue(result.data.size == 2)
        }

    @Test
    fun `does not crash when API fails`() =
        runTest(testDispatcher) {
            fakeInspectionsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(projectId)

            val result = fakeInspectionsDb.getInspectionsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppInspection>>>(result)
            assertTrue(result.data.isEmpty())
        }
}
