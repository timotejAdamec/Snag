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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import app.cash.turbine.test
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiState
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiStatus
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
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

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structure = FrontendStructure(
        structure = Structure(
            id = structureId,
            projectId = projectId,
            name = "Ground Floor",
            floorPlanUrl = null,
            updatedAt = Timestamp(10L),
        )
    )

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsApi) bind FindingsApi::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    private fun createViewModel() =
        StructureFloorPlanViewModel(
            structureId = structureId,
            getStructureUseCase = getStructureUseCase,
            deleteStructureUseCase = deleteStructureUseCase,
            getFindingsUseCase = getFindingsUseCase,
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
                assertEquals("Ground Floor", loadedState.feStructure?.structure?.name)

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
    fun `canInvokeDeletion is false while deleting`() =
        runTest(testDispatcher) {
            fakeStructuresDb.setStructure(structure)

            val viewModel = createViewModel()

            viewModel.state.test {
                // Skip initial loading state
                skipItems(1)

                val loadedState = awaitItem()
                assertEquals(true, loadedState.canInvokeDeletion)

                cancelAndIgnoreRemainingEvents()
            }

            // While loading
            assertEquals(StructureDetailsUiStatus.LOADING, StructureDetailsUiState().let {
                assertFalse(it.canInvokeDeletion)
                it.status
            })
        }
}
