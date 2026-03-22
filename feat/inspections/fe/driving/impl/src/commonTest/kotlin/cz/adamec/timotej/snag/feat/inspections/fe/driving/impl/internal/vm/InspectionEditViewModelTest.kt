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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionEditViewModelTest : FrontendKoinInitializedTest() {
    private val fakeInspectionsDb: FakeInspectionsDb by inject()

    private val getInspectionUseCase: GetInspectionUseCase by inject()
    private val saveInspectionUseCase: SaveInspectionUseCase by inject()
    private val deleteInspectionUseCase: DeleteInspectionUseCase by inject()
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase by inject()

    private fun createViewModel(
        inspectionId: Uuid? = null,
        projectId: Uuid? = null,
    ) = InspectionEditViewModel(
        inspectionId = inspectionId,
        projectId = projectId,
        getInspectionUseCase = getInspectionUseCase,
        saveInspectionUseCase = saveInspectionUseCase,
        deleteInspectionUseCase = deleteInspectionUseCase,
        canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
    )

    @Test
    fun `initial state is empty when creating with projectId provided and inspectionId null`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(inspectionId = null, projectId = projectId)

            assertEquals(null, viewModel.state.value.startedAt)
            assertEquals(null, viewModel.state.value.endedAt)
            assertEquals("", viewModel.state.value.participants)
            assertEquals("", viewModel.state.value.climate)
            assertEquals("", viewModel.state.value.note)
        }

    @Test
    fun `loading inspection data updates state when editing with inspectionId provided`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val inspection =
                AppInspectionData(
                    id = inspectionId,
                    projectId = projectId,
                    startedAt = Timestamp(100L),
                    endedAt = Timestamp(200L),
                    participants = "John Doe",
                    climate = "Sunny",
                    note = "Test note",
                    updatedAt = Timestamp(10L),
                )
            fakeInspectionsDb.setInspection(inspection)

            val viewModel = createViewModel(inspectionId = inspectionId, projectId = null)

            advanceUntilIdle()

            assertEquals(Timestamp(100L), viewModel.state.value.startedAt)
            assertEquals(Timestamp(200L), viewModel.state.value.endedAt)
            assertEquals("John Doe", viewModel.state.value.participants)
            assertEquals("Sunny", viewModel.state.value.climate)
            assertEquals("Test note", viewModel.state.value.note)
        }

    @Test
    fun `onParticipantsChange updates state`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onParticipantsChange("Jane Doe")

            assertEquals("Jane Doe", viewModel.state.value.participants)
        }

    @Test
    fun `onClimateChange updates state`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onClimateChange("Rainy")

            assertEquals("Rainy", viewModel.state.value.climate)
        }

    @Test
    fun `onNoteChange updates state`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onNoteChange("Some note")

            assertEquals("Some note", viewModel.state.value.note)
        }

    @Test
    fun `onStartedAtChange updates state`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onStartedAtChange(Timestamp(12_345L))

            assertEquals(Timestamp(12_345L), viewModel.state.value.startedAt)
        }

    @Test
    fun `onEndedAtChange updates state`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onEndedAtChange(Timestamp(67_890L))

            assertEquals(Timestamp(67_890L), viewModel.state.value.endedAt)
        }

    @Test
    fun `onSaveInspection successful in create mode sends save event`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            viewModel.onParticipantsChange("John Doe")
            viewModel.onClimateChange("Sunny")

            viewModel.onSaveInspection()

            val savedId = viewModel.saveEventFlow.first()

            val savedResult = fakeInspectionsDb.getInspectionFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<AppInspection?>>(savedResult)
            val saved = savedResult.data
            assertNotNull(saved)
            assertEquals("John Doe", saved.participants)
            assertEquals("Sunny", saved.climate)
            assertEquals(projectId, saved.projectId)
        }

    @Test
    fun `onSaveInspection successful in edit mode sends save event`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val inspection =
                AppInspectionData(
                    id = inspectionId,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = "Original",
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(10L),
                )
            fakeInspectionsDb.setInspection(inspection)

            val viewModel = createViewModel(inspectionId = inspectionId)

            advanceUntilIdle()

            viewModel.onParticipantsChange("Updated")
            viewModel.onSaveInspection()

            val savedId = viewModel.saveEventFlow.first()
            assertEquals(inspectionId, savedId)

            val savedResult = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<AppInspection?>>(savedResult)
            val saved = savedResult.data
            assertNotNull(saved)
            assertEquals("Updated", saved.participants)
            assertEquals(projectId, saved.projectId)
        }

    @Test
    fun `onSaveInspection failure sends error`() =
        runTest(testDispatcher) {
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

    @Test
    fun `onDelete successful sends deleted event`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val inspection =
                AppInspectionData(
                    id = inspectionId,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = "John Doe",
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(10L),
                )
            fakeInspectionsDb.setInspection(inspection)

            val viewModel = createViewModel(inspectionId = inspectionId)

            advanceUntilIdle()

            viewModel.onDelete()

            viewModel.deletedSuccessfullyEventFlow.first()

            val result = fakeInspectionsDb.getInspectionFlow(inspectionId).first()
            assertIs<OfflineFirstDataResult.Success<AppInspection?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `onDelete failure sends error and resets isBeingDeleted`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val inspectionId = UuidProvider.getUuid()
            val viewModel = createViewModel(inspectionId = inspectionId, projectId = projectId)

            fakeInspectionsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onDelete()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertEquals(true, viewModel.state.value.canEdit)
        }

    @Test
    fun `onDelete does nothing when inspectionId is null`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onDelete()

            advanceUntilIdle()

            assertEquals(true, viewModel.state.value.canEdit)
        }
}
