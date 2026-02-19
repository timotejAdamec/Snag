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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionEditViewModelTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()

    private val getInspectionUseCase: GetInspectionUseCase by inject()
    private val saveInspectionUseCase: SaveInspectionUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
            },
        )

    private fun createViewModel(
        inspectionId: Uuid? = null,
        projectId: Uuid? = null,
    ) = InspectionEditViewModel(
        inspectionId = inspectionId,
        projectId = projectId,
        getInspectionUseCase = getInspectionUseCase,
        saveInspectionUseCase = saveInspectionUseCase,
    )

    @Test
    fun `initial state is empty when creating with projectId provided and inspectionId null`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(inspectionId = null, projectId = projectId)

            assertEquals(null, viewModel.state.value.startedAt)
            assertEquals(null, viewModel.state.value.endedAt)
            assertEquals("", viewModel.state.value.participants)
            assertEquals("", viewModel.state.value.climate)
            assertEquals("", viewModel.state.value.note)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `loading inspection data updates state when editing with inspectionId provided`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val inspection =
                FrontendInspection(
                    Inspection(
                        id = inspectionId,
                        projectId = projectId,
                        startedAt = Timestamp(100L),
                        endedAt = Timestamp(200L),
                        participants = "John Doe",
                        climate = "Sunny",
                        note = "Test note",
                        updatedAt = Timestamp(10L),
                    ),
                )
            fakeInspectionsDb.setInspection(inspection)

            val viewModel = createViewModel(inspectionId = inspectionId, projectId = null)

            advanceUntilIdle()

            assertEquals(Timestamp(100L), viewModel.state.value.startedAt)
            assertEquals(Timestamp(200L), viewModel.state.value.endedAt)
            assertEquals("John Doe", viewModel.state.value.participants)
            assertEquals("Sunny", viewModel.state.value.climate)
            assertEquals("Test note", viewModel.state.value.note)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `onParticipantsChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onParticipantsChange("Jane Doe")

            assertEquals("Jane Doe", viewModel.state.value.participants)
        }

    @Test
    fun `onClimateChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onClimateChange("Rainy")

            assertEquals("Rainy", viewModel.state.value.climate)
        }

    @Test
    fun `onNoteChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onNoteChange("Some note")

            assertEquals("Some note", viewModel.state.value.note)
        }

    @Test
    fun `onStartedAtChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onStartedAtChange(Timestamp(12_345L))

            assertEquals(Timestamp(12_345L), viewModel.state.value.startedAt)
        }

    @Test
    fun `onEndedAtChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onEndedAtChange(Timestamp(67_890L))

            assertEquals(Timestamp(67_890L), viewModel.state.value.endedAt)
        }

    @Test
    fun `onSaveInspection successful in create mode sends save event`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            viewModel.onParticipantsChange("John Doe")
            viewModel.onClimateChange("Sunny")

            viewModel.onSaveInspection()

            val savedId = viewModel.saveEventFlow.first()

            val savedResult = fakeInspectionsDb.getInspectionFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendInspection?>>(savedResult)
            val saved = savedResult.data
            assertNotNull(saved)
            assertEquals("John Doe", saved.inspection.participants)
            assertEquals("Sunny", saved.inspection.climate)
            assertEquals(projectId, saved.inspection.projectId)
        }

    @Test
    fun `onSaveInspection successful in edit mode sends save event`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val inspection =
                FrontendInspection(
                    inspection =
                        Inspection(
                            id = inspectionId,
                            projectId = projectId,
                            startedAt = null,
                            endedAt = null,
                            participants = "Original",
                            climate = null,
                            note = null,
                            updatedAt = Timestamp(10L),
                        ),
                )
            fakeInspectionsDb.setInspection(inspection)

            val viewModel = createViewModel(inspectionId = inspectionId)

            advanceUntilIdle()

            viewModel.onParticipantsChange("Updated")
            viewModel.onSaveInspection()

            val savedId = viewModel.saveEventFlow.first()
            assertEquals(inspectionId, savedId)

            val savedResult = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendInspection?>>(savedResult)
            val saved = savedResult.data
            assertNotNull(saved)
            assertEquals("Updated", saved.inspection.participants)
            assertEquals(projectId, saved.inspection.projectId)
        }

    @Test
    fun `onSaveInspection failure sends error`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveInspection()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }

    @Test
    fun `constructor requires either inspectionId or projectId`() {
        assertFailsWith<IllegalArgumentException> {
            createViewModel(inspectionId = null, projectId = null)
        }
    }
}
