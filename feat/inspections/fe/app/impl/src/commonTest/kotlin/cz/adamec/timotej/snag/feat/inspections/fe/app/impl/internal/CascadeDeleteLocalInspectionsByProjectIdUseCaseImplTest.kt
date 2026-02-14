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
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeDeleteLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
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

class CascadeDeleteLocalInspectionsByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()

    private val useCase: CascadeDeleteLocalInspectionsByProjectIdUseCase by inject()

    private val projectId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val projectId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private val inspectionId1 = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val inspectionId2 = Uuid.parse("00000000-0000-0000-0001-000000000002")
    private val inspectionId3 = Uuid.parse("00000000-0000-0000-0001-000000000003")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
            },
        )

    private fun createInspection(
        id: Uuid,
        projectId: Uuid,
    ) = FrontendInspection(
        inspection =
            Inspection(
                id = id,
                projectId = projectId,
                startedAt = null,
                endedAt = null,
                participants = null,
                climate = null,
                note = null,
                updatedAt = Timestamp(1L),
            ),
    )

    @Test
    fun `deletes all inspections for the given project`() =
        runTest(testDispatcher) {
            val inspection1 = createInspection(id = inspectionId1, projectId = projectId1)
            val inspection2 = createInspection(id = inspectionId2, projectId = projectId1)
            fakeInspectionsDb.setInspections(listOf(inspection1, inspection2))

            useCase(projectId1)

            val result = fakeInspectionsDb.getInspectionsFlow(projectId1).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendInspection>>>(result)
            assertTrue(result.data.isEmpty())
        }

    @Test
    fun `does not delete inspections from other projects`() =
        runTest(testDispatcher) {
            val inspectionForProject1 = createInspection(id = inspectionId1, projectId = projectId1)
            val inspectionForProject2 = createInspection(id = inspectionId3, projectId = projectId2)
            fakeInspectionsDb.setInspections(listOf(inspectionForProject1, inspectionForProject2))

            useCase(projectId1)

            val result = fakeInspectionsDb.getInspectionsFlow(projectId2).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendInspection>>>(result)
            assertTrue(result.data.size == 1)
        }
}
