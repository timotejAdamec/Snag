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

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.GetFindingsUseCaseImpl
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.DeleteStructureUseCaseImpl
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.GetStructureUseCaseImpl
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiState
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureDetailsUiStatus
import cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm.StructureFloorPlanViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StructureDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val applicationScope =
        object : ApplicationScope, CoroutineScope by CoroutineScope(testDispatcher) {}

    private val structuresDb = FakeStructuresDb()
    private val structuresSync = FakeStructuresSync()
    private val findingsDb = FakeFindingsDb()
    private val findingsApi = FakeFindingsApi()

    private val getStructureUseCase = GetStructureUseCaseImpl(structuresDb)
    private val deleteStructureUseCase = DeleteStructureUseCaseImpl(structuresDb, structuresSync)
    private val getFindingsUseCase = GetFindingsUseCaseImpl(
        findingsDb = findingsDb,
        findingsApi = findingsApi,
        applicationScope = applicationScope,
    )

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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() =
        runTest {
            val viewModel = createViewModel()

            assertEquals(StructureDetailsUiStatus.LOADING, viewModel.state.value.status)
        }

    @Test
    fun `loading structure data updates state`() =
        runTest {
            structuresDb.setStructure(structure)

            val viewModel = createViewModel()

            advanceUntilIdle()

            assertEquals(StructureDetailsUiStatus.LOADED, viewModel.state.value.status)
            assertNotNull(viewModel.state.value.feStructure)
            assertEquals("Ground Floor", viewModel.state.value.feStructure?.structure?.name)
        }

    @Test
    fun `structure not found updates status`() =
        runTest {
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertEquals(StructureDetailsUiStatus.NOT_FOUND, viewModel.state.value.status)
        }

    @Test
    fun `onDelete success triggers deleted event`() =
        runTest {
            structuresDb.setStructure(structure)

            val viewModel = createViewModel()

            advanceUntilIdle()

            viewModel.onDelete()

            val event = viewModel.deletedSuccessfullyEventFlow.first()
            assertEquals(Unit, event)
            assertEquals(StructureDetailsUiStatus.DELETED, viewModel.state.value.status)
            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `onDelete failure sends error and resets isBeingDeleted`() =
        runTest {
            structuresDb.setStructure(structure)

            val viewModel = createViewModel()

            advanceUntilIdle()

            structuresDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onDelete()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `canInvokeDeletion is false while deleting`() =
        runTest {
            structuresDb.setStructure(structure)

            val viewModel = createViewModel()

            advanceUntilIdle()

            // Before deletion
            assertEquals(true, viewModel.state.value.canInvokeDeletion)

            // While loading
            assertEquals(StructureDetailsUiStatus.LOADING, StructureDetailsUiState().let {
                assertFalse(it.canInvokeDeletion)
                it.status
            })
        }

    private fun createViewModel() =
        StructureFloorPlanViewModel(
            structureId = structureId,
            getStructureUseCase = getStructureUseCase,
            deleteStructureUseCase = deleteStructureUseCase,
            getFindingsUseCase = getFindingsUseCase,
        )
}
