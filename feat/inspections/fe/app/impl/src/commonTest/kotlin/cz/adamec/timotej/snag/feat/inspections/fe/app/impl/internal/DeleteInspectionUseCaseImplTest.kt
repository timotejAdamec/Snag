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
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteInspectionUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakeInspectionsSync: FakeInspectionsSync by inject()

    private val useCase: DeleteInspectionUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val inspectionId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
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
    fun `deletes inspection from db`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsDb.setInspection(inspection)

            useCase(inspectionId)

            val result = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendInspection?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val inspection = createInspection(inspectionId)
            fakeInspectionsDb.setInspection(inspection)

            useCase(inspectionId)

            assertEquals(listOf(inspectionId), fakeInspectionsSync.deletedInspectionIds)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(inspectionId)

            assertTrue(fakeInspectionsSync.deletedInspectionIds.isEmpty())
        }
}
