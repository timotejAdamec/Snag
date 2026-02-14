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

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.model.SaveInspectionRequest
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SaveInspectionUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakeInspectionsSync: FakeInspectionsSync by inject()

    private val useCase: SaveInspectionUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
            },
        )

    @Test
    fun `saves inspection and enqueues sync`() =
        runTest(testDispatcher) {
            val request =
                SaveInspectionRequest(
                    id = null,
                    projectId = projectId,
                    startedAt = Timestamp(100L),
                    endedAt = null,
                    participants = "John Doe",
                    climate = "Sunny",
                    note = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertEquals(1, fakeInspectionsSync.savedInspectionIds.size)
            assertEquals(result.data, fakeInspectionsSync.savedInspectionIds.first())
        }

    @Test
    fun `saved inspection has correct fields`() =
        runTest(testDispatcher) {
            val request =
                SaveInspectionRequest(
                    id = null,
                    projectId = projectId,
                    startedAt = Timestamp(100L),
                    endedAt = Timestamp(200L),
                    participants = "Jane Doe",
                    climate = "Rainy",
                    note = "Test note",
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val saved = getSavedInspection(result.data)
            assertEquals(projectId, saved.inspection.projectId)
            assertEquals(Timestamp(100L), saved.inspection.startedAt)
            assertEquals(Timestamp(200L), saved.inspection.endedAt)
            assertEquals("Jane Doe", saved.inspection.participants)
            assertEquals("Rainy", saved.inspection.climate)
            assertEquals("Test note", saved.inspection.note)
        }

    @Test
    fun `uses provided id when present`() =
        runTest(testDispatcher) {
            val id = Uuid.parse("00000000-0000-0000-0001-000000000001")
            val request =
                SaveInspectionRequest(
                    id = id,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = null,
                    climate = null,
                    note = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertEquals(id, result.data)
        }

    @Test
    fun `generates new id when id is null`() =
        runTest(testDispatcher) {
            val request =
                SaveInspectionRequest(
                    id = null,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = null,
                    climate = null,
                    note = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            assertNotNull(result.data)
        }

    @Test
    fun `returns error when db save fails`() =
        runTest(testDispatcher) {
            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveInspectionRequest(
                    id = null,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = null,
                    climate = null,
                    note = null,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.ProgrammerError>(result)
        }

    @Test
    fun `does not enqueue sync when save fails`() =
        runTest(testDispatcher) {
            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Save error"))

            val request =
                SaveInspectionRequest(
                    id = null,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = null,
                    climate = null,
                    note = null,
                )

            useCase(request)

            assertTrue(fakeInspectionsSync.savedInspectionIds.isEmpty())
        }

    private suspend fun getSavedInspection(id: Uuid): FrontendInspection {
        fakeInspectionsDb.forcedFailure = null
        val result = fakeInspectionsDb.getInspectionFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
