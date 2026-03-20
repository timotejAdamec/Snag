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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm

import app.cash.turbine.test
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.IsProjectClosedUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiState
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiStatus
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StructureDetailsViewModelTest : FrontendKoinInitializedTest() {
    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val getStructureUseCase: GetStructureUseCase by inject()
    private val deleteStructureUseCase: DeleteStructureUseCase by inject()
    private val getFindingsUseCase: GetFindingsUseCase by inject()
    private val isProjectClosedUseCase: IsProjectClosedUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structure =
        AppStructureData(
            id = structureId,
            projectId = projectId,
            name = "Ground Floor",
            floorPlanUrl = null,
            updatedAt = Timestamp(10L),
        )

    private fun createViewModel() =
        StructureFloorPlanViewModel(
            structureId = structureId,
            projectId = projectId,
            getStructureUseCase = getStructureUseCase,
            deleteStructureUseCase = deleteStructureUseCase,
            getFindingsUseCase = getFindingsUseCase,
            isProjectClosedUseCase = isProjectClosedUseCase,
        )

    @Test
    fun `initial state is loading`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(StructureDetailsUiStatus.LOADING, viewModel.state.value.status)
        }

    @Test
    fun `loading structure data updates state`() =
        runTest(testDispatcher) {
            fakeStructuresDb.setStructure(structure)

            val viewModel = createViewModel()

            viewModel.state.test {
                // Skip initial loading state
                skipItems(1)

                val loadedState = awaitItem()
                assertEquals(StructureDetailsUiStatus.LOADED, loadedState.status)
                assertNotNull(loadedState.feStructure)
                assertEquals("Ground Floor", loadedState.feStructure?.name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `structure not found updates status`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.state.test {
                // Skip initial loading state
                skipItems(1)

                val notFoundState = awaitItem()
                assertEquals(StructureDetailsUiStatus.NOT_FOUND, notFoundState.status)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDelete success triggers deleted event`() =
        runTest(testDispatcher) {
            fakeStructuresDb.setStructure(structure)

            val viewModel = createViewModel()

            viewModel.state.test {
                // Wait for loaded state
                skipItems(1)
                awaitItem() // LOADED state

                viewModel.onDelete()

                // Verify deleted event
                viewModel.deletedSuccessfullyEventFlow.test {
                    assertEquals(Unit, awaitItem())
                    cancelAndIgnoreRemainingEvents()
                }

                val deletedState = awaitItem()
                assertEquals(StructureDetailsUiStatus.DELETED, deletedState.status)
                assertFalse(deletedState.isBeingDeleted)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDelete failure sends error and resets isBeingDeleted`() =
        runTest(testDispatcher) {
            fakeStructuresDb.setStructure(structure)

            val viewModel = createViewModel()

            viewModel.state.test {
                // Wait for loaded state
                skipItems(1)
                awaitItem() // LOADED state

                fakeStructuresDb.forcedFailure =
                    OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

                viewModel.onDelete()

                viewModel.errorsFlow.test {
                    assertIs<UiError.Unknown>(awaitItem())
                    cancelAndIgnoreRemainingEvents()
                }

                cancelAndIgnoreRemainingEvents()
            }

            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `canEdit is false while deleting`() =
        runTest(testDispatcher) {
            fakeStructuresDb.setStructure(structure)

            val viewModel = createViewModel()

            viewModel.state.test {
                // Skip initial loading state
                skipItems(1)

                val loadedState = awaitItem()
                assertEquals(true, loadedState.canEdit)

                cancelAndIgnoreRemainingEvents()
            }

            // While loading
            assertEquals(
                StructureDetailsUiStatus.LOADING,
                StructureDetailsUiState().let {
                    assertFalse(it.canEdit)
                    it.status
                },
            )
        }
}
