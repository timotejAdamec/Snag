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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm

import app.cash.turbine.test
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class FindingDetailViewModelTest : FrontendKoinInitializedTest() {
    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val getFindingUseCase: GetFindingUseCase by inject()
    private val deleteFindingUseCase: DeleteFindingUseCase by inject()
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase by inject()
    private val getFindingPhotosUseCase: GetFindingPhotosUseCase by inject()
    private val deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val finding =
        AppFindingData(
            id = findingId,
            structureId = structureId,
            name = "Crack in wall",
            description = "A large crack",
            type = FindingType.Classic(),
            coordinates = emptySet(),
            updatedAt = Timestamp(10L),
        )

    private fun createViewModel(): FindingDetailViewModel =
        TestFindingDetailViewModel(
            findingId = findingId,
            projectId = projectId,
            getFindingUseCase = getFindingUseCase,
            deleteFindingUseCase = deleteFindingUseCase,
            canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
            getFindingPhotosUseCase = getFindingPhotosUseCase,
            deleteFindingPhotoUseCase = deleteFindingPhotoUseCase,
        )

    @Test
    fun `initial state is loading`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(FindingDetailUiStatus.LOADING, viewModel.state.value.status)
        }

    @Test
    fun `loading finding data updates state`() =
        runTest(testDispatcher) {
            fakeFindingsDb.setFinding(finding)

            val viewModel = createViewModel()

            viewModel.state.test {
                skipItems(1) // Skip initial loading state

                val loadedState = awaitItem()
                assertEquals(FindingDetailUiStatus.LOADED, loadedState.status)
                assertNotNull(loadedState.finding)
                assertEquals("Crack in wall", loadedState.finding.name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `finding not found updates status`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.state.test {
                skipItems(1) // Skip initial loading state

                val notFoundState = awaitItem()
                assertEquals(FindingDetailUiStatus.NOT_FOUND, notFoundState.status)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDelete success triggers deleted event`() =
        runTest(testDispatcher) {
            fakeFindingsDb.setFinding(finding)

            val viewModel = createViewModel()

            advanceUntilIdle()

            viewModel.onDelete()

            // Verify deleted event is sent
            val event = viewModel.deletedSuccessfullyEventFlow.first()
            assertEquals(Unit, event)
            assertEquals(FindingDetailUiStatus.DELETED, viewModel.state.value.status)
            assertFalse(viewModel.state.value.canEdit)
        }

    @Test
    fun `onDelete failure sends error and resets isBeingDeleted`() =
        runTest(testDispatcher) {
            fakeFindingsDb.setFinding(finding)

            val viewModel = createViewModel()

            advanceUntilIdle()

            fakeFindingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onDelete()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertTrue(viewModel.state.value.canEdit)
        }

    @Test
    fun `canEdit is false while loading`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(FindingDetailUiStatus.LOADING, viewModel.state.value.status)
            assertFalse(viewModel.state.value.canEdit)
        }
}

private class TestFindingDetailViewModel(
    findingId: Uuid,
    projectId: Uuid,
    getFindingUseCase: GetFindingUseCase,
    deleteFindingUseCase: DeleteFindingUseCase,
    canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
    getFindingPhotosUseCase: GetFindingPhotosUseCase,
    deleteFindingPhotoUseCase: DeleteFindingPhotoUseCase,
) : FindingDetailViewModel(
    findingId = findingId,
    projectId = projectId,
    getFindingUseCase = getFindingUseCase,
    deleteFindingUseCase = deleteFindingUseCase,
    canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
    getFindingPhotosUseCase = getFindingPhotosUseCase,
    deleteFindingPhotoUseCase = deleteFindingPhotoUseCase,
) {
    override fun onAddPhoto(
        bytes: ByteArray,
        fileName: String,
    ) {
        // No-op for testing base class functionality
    }
}
