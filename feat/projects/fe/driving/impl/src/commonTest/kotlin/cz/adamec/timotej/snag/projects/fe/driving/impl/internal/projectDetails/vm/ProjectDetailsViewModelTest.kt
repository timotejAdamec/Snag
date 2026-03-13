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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.driven.test.FakeReportsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsViewModelTest : FrontendKoinInitializedTest() {
    private val fixedNow = Timestamp(1_700_000_000_000L)

    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakeInspectionsApi: FakeInspectionsApi by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val fakeReportsApi: FakeReportsApi by inject()

    private val getProjectUseCase: GetProjectUseCase by inject()
    private val deleteProjectUseCase: DeleteProjectUseCase by inject()
    private val getStructuresUseCase: GetStructuresUseCase by inject()
    private val getInspectionsUseCase: GetInspectionsUseCase by inject()
    private val saveInspectionUseCase: SaveInspectionUseCase by inject()
    private val downloadReportUseCase: DownloadReportUseCase by inject()
    private val setProjectClosedUseCase: SetProjectClosedUseCase by inject()
    private val timestampProvider: TimestampProvider by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                single<TimestampProvider> {
                    object : TimestampProvider {
                        override fun getNowTimestamp() = fixedNow
                    }
                }
            },
        )

    private fun createViewModel(projectId: Uuid) =
        ProjectDetailsViewModel(
            projectId = projectId,
            getProjectUseCase = getProjectUseCase,
            deleteProjectUseCase = deleteProjectUseCase,
            getStructuresUseCase = getStructuresUseCase,
            getInspectionsUseCase = getInspectionsUseCase,
            downloadReportUseCase = downloadReportUseCase,
            saveInspectionUseCase = saveInspectionUseCase,
            setProjectClosedUseCase = setProjectClosedUseCase,
            timestampProvider = timestampProvider,
        )

    private fun seedInspection(
        projectId: Uuid,
        inspectionId: Uuid = Uuid.random(),
        startedAt: Timestamp? = null,
        endedAt: Timestamp? = null,
        participants: String? = "Alice",
        climate: String? = "sunny",
        note: String? = "note",
    ): FrontendInspection {
        val inspection =
            FrontendInspection(
                inspection =
                    Inspection(
                        id = inspectionId,
                        projectId = projectId,
                        startedAt = startedAt,
                        endedAt = endedAt,
                        participants = participants,
                        climate = climate,
                        note = note,
                        updatedAt = Timestamp(10L),
                    ),
            )
        fakeInspectionsDb.setInspection(inspection)
        return inspection
    }

    private fun seedProject(projectId: Uuid): FrontendProject {
        val project =
            FrontendProject(
                project =
                    Project(
                        id = projectId,
                        name = "Test Project",
                        address = "Test Address",
                        updatedAt = Timestamp(10L),
                    ),
            )
        fakeProjectsDb.setProject(project)
        return project
    }

    @Test
    fun `downloading report on success sends report to reportReadyFlow`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            val samplePdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)
            fakeReportsApi.reportBytes = samplePdfBytes
            fakeReportsApi.reportFileName = "Test_Project_Report.pdf"

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onDownloadReport()

            val report = viewModel.reportReadyFlow.first()
            assertTrue(report.report.bytes.contentEquals(samplePdfBytes))
            assertEquals("Test_Project_Report.pdf", report.report.fileName)
            assertFalse(viewModel.state.value.isDownloadingReport)
        }

    @Test
    fun `downloading report on network failure shows network error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            fakeReportsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onDownloadReport()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.NetworkUnavailable>(error)
            assertFalse(viewModel.state.value.isDownloadingReport)
        }

    @Test
    fun `downloading report on generic failure shows unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            fakeReportsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("fail"))

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onDownloadReport()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertFalse(viewModel.state.value.isDownloadingReport)
        }

    @Test
    fun `downloading report calls use case with correct projectId`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onDownloadReport()
            advanceUntilIdle()

            assertEquals(listOf(projectId), fakeReportsApi.downloadedProjectIds)
        }

    @Test
    fun `canDownloadReport is true when loaded and not downloading`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDownloadReport)
            assertEquals(ProjectDetailsUiStatus.LOADED, viewModel.state.value.projectStatus)
        }

    @Test
    fun `canDownloadReport is false when project is not loaded`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()

            val viewModel = createViewModel(projectId)

            assertFalse(viewModel.state.value.canDownloadReport)
        }

    @Test
    fun `canDownloadReport is false while downloading`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            val deferred = CompletableDeferred<Unit>()
            fakeReportsApi.downloadDeferred = deferred

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.canDownloadReport)

            val reportCollector = launch { viewModel.reportReadyFlow.first() }
            viewModel.onDownloadReport()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.canDownloadReport)
            assertTrue(viewModel.state.value.isDownloadingReport)

            deferred.complete(Unit)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDownloadReport)
            assertFalse(viewModel.state.value.isDownloadingReport)
            reportCollector.cancel()
        }

    @Test
    fun `onStartInspection sets startedAt to current timestamp`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.inspection.id == inspectionId }
            assertEquals(fixedNow, saved?.inspection?.startedAt)
        }

    @Test
    fun `onStartInspection preserves existing fields`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(
                projectId = projectId,
                inspectionId = inspectionId,
                endedAt = Timestamp(999L),
                participants = "Bob",
                climate = "rainy",
                note = "my note",
            )

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.inspection.id == inspectionId }
            val insp = saved?.inspection
            assertEquals(projectId, insp?.projectId)
            assertEquals(Timestamp(999L), insp?.endedAt)
            assertEquals("Bob", insp?.participants)
            assertEquals("rainy", insp?.climate)
            assertEquals("my note", insp?.note)
        }

    @Test
    fun `onEndInspection sets endedAt to current timestamp`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId, startedAt = Timestamp(1L))

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.inspection.id == inspectionId }
            assertEquals(fixedNow, saved?.inspection?.endedAt)
        }

    @Test
    fun `onEndInspection preserves existing startedAt`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            val existingStartedAt = Timestamp(500L)
            seedProject(projectId)
            seedInspection(
                projectId = projectId,
                inspectionId = inspectionId,
                startedAt = existingStartedAt,
            )

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.inspection.id == inspectionId }
            assertEquals(existingStartedAt, saved?.inspection?.startedAt)
        }

    @Test
    fun `onStartInspection does nothing for unknown inspection id`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }

    @Test
    fun `onEndInspection does nothing for unknown inspection id`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }

    @Test
    fun `onStartInspection syncs the inspection`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            val apiResult = fakeInspectionsApi.getInspections(projectId)
            assertIs<OnlineDataResult.Success<List<FrontendInspection>>>(apiResult)
            val synced = apiResult.data.find { it.inspection.id == inspectionId }
            assertEquals(fixedNow, synced?.inspection?.startedAt)
        }

    @Test
    fun `onEndInspection syncs the inspection`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId, startedAt = Timestamp(1L))

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            val apiResult = fakeInspectionsApi.getInspections(projectId)
            assertIs<OnlineDataResult.Success<List<FrontendInspection>>>(apiResult)
            val synced = apiResult.data.find { it.inspection.id == inspectionId }
            assertEquals(fixedNow, synced?.inspection?.endedAt)
        }

    private fun seedClosedProject(projectId: Uuid): FrontendProject {
        val project =
            FrontendProject(
                project =
                    Project(
                        id = projectId,
                        name = "Closed Project",
                        address = "Test Address",
                        isClosed = true,
                        updatedAt = Timestamp(10L),
                    ),
            )
        fakeProjectsDb.setProject(project)
        return project
    }

    @Test
    fun `onToggleClose on open project sets isClosed to true`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isClosed)

            viewModel.onToggleClose()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isClosed)
        }

    @Test
    fun `onToggleClose on closed project sets isClosed to false`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedClosedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isClosed)

            viewModel.onToggleClose()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isClosed)
        }

    @Test
    fun `onToggleClose network failure sends error`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedProject(projectId)
            val fakeProjectsApi: FakeProjectsApi by inject()
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onToggleClose()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.NetworkUnavailable>(error)
        }

    @Test
    fun `canToggleClosed is false when not loaded`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()

            val viewModel = createViewModel(projectId)

            assertFalse(viewModel.state.value.canToggleClosed)
        }
}
