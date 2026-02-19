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
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.driven.test.FakeReportsApi
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsPullSyncCoordinator
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsSync
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncCoordinator
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresApi
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsViewModelTest : FrontendKoinInitializedTest() {
    private val fixedNow = Timestamp(1_700_000_000_000L)

    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeInspectionsDb: FakeInspectionsDb by inject()
    private val fakeInspectionsSync: FakeInspectionsSync by inject()
    private val fakeReportsApi: FakeReportsApi by inject()

    private val getProjectUseCase: GetProjectUseCase by inject()
    private val deleteProjectUseCase: DeleteProjectUseCase by inject()
    private val getStructuresUseCase: GetStructuresUseCase by inject()
    private val getInspectionsUseCase: GetInspectionsUseCase by inject()
    private val saveInspectionUseCase: SaveInspectionUseCase by inject()
    private val downloadReportUseCase: DownloadReportUseCase by inject()
    private val timestampProvider: TimestampProvider by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
                singleOf(::FakeProjectsPullSyncCoordinator) bind ProjectsPullSyncCoordinator::class
                singleOf(::FakeProjectsPullSyncTimestampDataSource) bind ProjectsPullSyncTimestampDataSource::class
                singleOf(::FakeStructuresApi) bind StructuresApi::class
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                singleOf(::FakeReportsApi) bind ReportsApi::class
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
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDownloadReport)
            assertEquals(ProjectDetailsUiStatus.LOADED, viewModel.state.value.projectStatus)
        }

    @Test
    fun `canDownloadReport is false when project is not loaded`() =
        runTest {
            val projectId = Uuid.random()

            val viewModel = createViewModel(projectId)

            assertFalse(viewModel.state.value.canDownloadReport)
        }

    @Test
    fun `canDownloadReport is false while downloading`() =
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
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
        runTest {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeInspectionsSync.savedInspectionIds.isEmpty())
        }

    @Test
    fun `onEndInspection does nothing for unknown inspection id`() =
        runTest {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeInspectionsSync.savedInspectionIds.isEmpty())
        }

    @Test
    fun `onStartInspection enqueues sync for the inspection`() =
        runTest {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId)

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            assertEquals(listOf(inspectionId), fakeInspectionsSync.savedInspectionIds)
        }

    @Test
    fun `onEndInspection enqueues sync for the inspection`() =
        runTest {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId, startedAt = Timestamp(1L))

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            assertEquals(listOf(inspectionId), fakeInspectionsSync.savedInspectionIds)
        }
}
