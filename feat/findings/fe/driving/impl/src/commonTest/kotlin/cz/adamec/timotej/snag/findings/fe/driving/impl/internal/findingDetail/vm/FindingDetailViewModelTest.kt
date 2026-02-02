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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.DeleteFindingUseCaseImpl
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.GetFindingUseCaseImpl
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
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
class FindingDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val findingsDb = FakeFindingsDb()
    private val findingsSync = FakeFindingsSync()

    private val getFindingUseCase = GetFindingUseCaseImpl(findingsDb)
    private val deleteFindingUseCase = DeleteFindingUseCaseImpl(findingsDb, findingsSync)

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val finding = Finding(findingId, structureId, "Crack in wall", "A large crack", emptyList())

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

            assertEquals(FindingDetailUiStatus.LOADING, viewModel.state.value.status)
        }

    @Test
    fun `loading finding data updates state`() =
        runTest {
            findingsDb.setFinding(finding)

            val viewModel = createViewModel()

            advanceUntilIdle()

            assertEquals(FindingDetailUiStatus.LOADED, viewModel.state.value.status)
            assertNotNull(viewModel.state.value.finding)
            assertEquals("Crack in wall", viewModel.state.value.finding?.name)
        }

    @Test
    fun `finding not found updates status`() =
        runTest {
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertEquals(FindingDetailUiStatus.NOT_FOUND, viewModel.state.value.status)
        }

    @Test
    fun `onDelete success triggers deleted event`() =
        runTest {
            findingsDb.setFinding(finding)

            val viewModel = createViewModel()

            advanceUntilIdle()

            viewModel.onDelete()

            val event = viewModel.deletedSuccessfullyEventFlow.first()
            assertEquals(Unit, event)
            assertEquals(FindingDetailUiStatus.DELETED, viewModel.state.value.status)
            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `onDelete failure sends error and resets isBeingDeleted`() =
        runTest {
            findingsDb.setFinding(finding)

            val viewModel = createViewModel()

            advanceUntilIdle()

            findingsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onDelete()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `canInvokeDeletion is false while loading`() =
        runTest {
            val viewModel = createViewModel()

            assertEquals(FindingDetailUiStatus.LOADING, viewModel.state.value.status)
            assertFalse(viewModel.state.value.canInvokeDeletion)
        }

    private fun createViewModel() =
        FindingDetailViewModel(
            findingId = findingId,
            getFindingUseCase = getFindingUseCase,
            deleteFindingUseCase = deleteFindingUseCase,
        )
}
