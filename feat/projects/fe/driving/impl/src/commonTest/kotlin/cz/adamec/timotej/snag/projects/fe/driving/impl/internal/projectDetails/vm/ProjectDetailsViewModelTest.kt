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

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.driven.test.FakeReportsApi
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
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
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsViewModelTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeReportsApi: FakeReportsApi by inject()

    private val getProjectUseCase: GetProjectUseCase by inject()
    private val deleteProjectUseCase: DeleteProjectUseCase by inject()
    private val getStructuresUseCase: GetStructuresUseCase by inject()
    private val getInspectionsUseCase: GetInspectionsUseCase by inject()
    private val downloadReportUseCase: DownloadReportUseCase by inject()

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
        )

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
    fun `downloading report on success sends bytes to reportReadyFlow`() =
        runTest {
            val projectId = Uuid.random()
            seedProject(projectId)
            val samplePdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)
            fakeReportsApi.reportBytes = samplePdfBytes

            val viewModel = createViewModel(projectId)
            advanceUntilIdle()

            viewModel.onDownloadReport()

            val (bytes, baseName) = viewModel.reportReadyFlow.first()
            assertTrue(bytes.contentEquals(samplePdfBytes))
            assertTrue(baseName.contains("Test"))
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
}
