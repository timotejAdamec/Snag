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
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class FindingDetailViewModelTest : FrontendKoinInitializedTest() {

    private val fakeFindingsDb: FakeFindingsDb by inject()

    private val getFindingUseCase: GetFindingUseCase by inject()
    private val deleteFindingUseCase: DeleteFindingUseCase by inject()

    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val finding = Finding(findingId, structureId, "Crack in wall", "A large crack", emptyList())

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeFindingsSync) bind FindingsSync::class
            },
        )

    private fun createViewModel() =
        FindingDetailViewModel(
            findingId = findingId,
            getFindingUseCase = getFindingUseCase,
            deleteFindingUseCase = deleteFindingUseCase,
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
                assertEquals("Crack in wall", loadedState.finding?.name)

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
            assertFalse(viewModel.state.value.isBeingDeleted)
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
            assertFalse(viewModel.state.value.isBeingDeleted)
        }

    @Test
    fun `canInvokeDeletion is false while loading`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(FindingDetailUiStatus.LOADING, viewModel.state.value.status)
            assertFalse(viewModel.state.value.canInvokeDeletion)
        }
}
