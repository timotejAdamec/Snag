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

package cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectDetails.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.app.api.GetAvailableReportTypesFlowUseCase
import cz.adamec.timotej.snag.feat.reports.fe.driven.test.FakeReportsApi
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.UpdateProjectPhotoDescriptionUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
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
    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")

    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val fakeUsersDb: FakeUsersDb by inject()
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
    private val getAvailableReportTypesUseCase: GetAvailableReportTypesFlowUseCase by inject()
    private val setProjectClosedUseCase: SetProjectClosedUseCase by inject()
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase by inject()
    private val canCloseProjectUseCase: CanCloseProjectUseCase by inject()
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase by inject()
    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase by inject()
    private val getUsersUseCase: GetUsersUseCase by inject()
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase by inject()
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase by inject()
    private val timestampProvider: TimestampProvider by inject()
    private val getProjectPhotosUseCase: GetProjectPhotosUseCase by inject()
    private val deleteProjectPhotoUseCase: DeleteProjectPhotoUseCase by inject()
    private val updateProjectPhotoDescriptionUseCase: UpdateProjectPhotoDescriptionUseCase by inject()

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

    private fun seedCurrentUser() {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                authProviderId = "mock-auth-provider-id",
                email = "admin@test.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = Timestamp(0L),
            ),
        )
    }

    private fun createViewModel(projectId: Uuid): ProjectDetailsViewModel =
        object : ProjectDetailsViewModel(
            projectId = projectId,
            getProjectUseCase = getProjectUseCase,
            deleteProjectUseCase = deleteProjectUseCase,
            getStructuresUseCase = getStructuresUseCase,
            getInspectionsUseCase = getInspectionsUseCase,
            downloadReportUseCase = downloadReportUseCase,
            getAvailableReportTypesUseCase = getAvailableReportTypesUseCase,
            saveInspectionUseCase = saveInspectionUseCase,
            setProjectClosedUseCase = setProjectClosedUseCase,
            canEditProjectEntitiesUseCase = canEditProjectEntitiesUseCase,
            canCloseProjectUseCase = canCloseProjectUseCase,
            canAssignUserToProjectUseCase = canAssignUserToProjectUseCase,
            getProjectAssignmentsUseCase = getProjectAssignmentsUseCase,
            getUsersUseCase = getUsersUseCase,
            assignUserToProjectUseCase = assignUserToProjectUseCase,
            removeUserFromProjectUseCase = removeUserFromProjectUseCase,
            timestampProvider = timestampProvider,
            getProjectPhotosUseCase = getProjectPhotosUseCase,
            deleteProjectPhotoUseCase = deleteProjectPhotoUseCase,
            updateProjectPhotoDescriptionUseCase = updateProjectPhotoDescriptionUseCase,
        ) {
            override fun onAddPhoto(
                bytes: ByteArray,
                fileName: String,
                description: String,
            ) {
                // no-op for existing tests
            }
        }

    private fun seedInspection(
        projectId: Uuid,
        inspectionId: Uuid = Uuid.random(),
        dateFrom: Timestamp? = null,
        dateTo: Timestamp? = null,
        participants: String? = "Alice",
        climate: String? = "sunny",
        note: String? = "note",
    ): AppInspection {
        val inspection =
            AppInspectionData(
                id = inspectionId,
                projectId = projectId,
                dateFrom = dateFrom,
                dateTo = dateTo,
                participants = participants,
                climate = climate,
                note = note,
                updatedAt = Timestamp(10L),
            )
        fakeInspectionsDb.setInspection(inspection)
        return inspection
    }

    private fun seedProject(projectId: Uuid): AppProject {
        seedCurrentUser()
        val project =
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = UuidProvider.getUuid(),
                updatedAt = Timestamp(10L),
            )
        fakeProjectsDb.setProject(project)
        fakeProjectAssignmentsDb.setAssignments(projectId, setOf(currentUserId))
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
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onDownloadReport(ReportType.PASSPORT)

            val report = viewModel.reportReadyFlow.first()
            assertTrue(report.bytes.contentEquals(samplePdfBytes))
            assertEquals("Test_Project_Report.pdf", report.fileName)
            assertFalse(viewModel.state.value.isDownloadingReport)
            subscriber.cancel()
        }

    @Test
    fun `downloading report on network failure shows network error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            fakeReportsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onDownloadReport(ReportType.PASSPORT)

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.NetworkUnavailable>(error)
            assertFalse(viewModel.state.value.isDownloadingReport)
            subscriber.cancel()
        }

    @Test
    fun `downloading report on generic failure shows unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)
            fakeReportsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("fail"))

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onDownloadReport(ReportType.PASSPORT)

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertFalse(viewModel.state.value.isDownloadingReport)
            subscriber.cancel()
        }

    @Test
    fun `downloading report calls use case with correct projectId`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onDownloadReport(ReportType.PASSPORT)
            advanceUntilIdle()

            assertEquals(listOf(projectId), fakeReportsApi.downloadedProjectIds)
            subscriber.cancel()
        }

    @Test
    fun `canDownloadReport is true when loaded and not downloading`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDownloadReport)
            assertEquals(ProjectDetailsUiStatus.LOADED, viewModel.state.value.projectStatus)
            subscriber.cancel()
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
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()
            assertTrue(viewModel.state.value.canDownloadReport)

            val reportCollector = launch { viewModel.reportReadyFlow.first() }
            viewModel.onDownloadReport(ReportType.PASSPORT)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.canDownloadReport)
            assertTrue(viewModel.state.value.isDownloadingReport)

            deferred.complete(Unit)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.canDownloadReport)
            assertFalse(viewModel.state.value.isDownloadingReport)
            reportCollector.cancel()
            subscriber.cancel()
        }

    @Test
    fun `onStartInspection sets dateFrom to current timestamp`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.id == inspectionId }
            assertEquals(fixedNow, saved?.dateFrom)
            subscriber.cancel()
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
                dateTo = Timestamp(999L),
                participants = "Bob",
                climate = "rainy",
                note = "my note",
            )

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.id == inspectionId }
            assertEquals(projectId, saved?.projectId)
            assertEquals(Timestamp(999L), saved?.dateTo)
            assertEquals("Bob", saved?.participants)
            assertEquals("rainy", saved?.climate)
            assertEquals("my note", saved?.note)
            subscriber.cancel()
        }

    @Test
    fun `onEndInspection sets dateTo to current timestamp`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId, dateFrom = Timestamp(1L))

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.id == inspectionId }
            assertEquals(fixedNow, saved?.dateTo)
            subscriber.cancel()
        }

    @Test
    fun `onEndInspection preserves existing dateFrom`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            val existingStartedAt = Timestamp(500L)
            seedProject(projectId)
            seedInspection(
                projectId = projectId,
                inspectionId = inspectionId,
                dateFrom = existingStartedAt,
            )

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            val saved =
                viewModel.state.value.inspections
                    .find { it.id == inspectionId }
            assertEquals(existingStartedAt, saved?.dateFrom)
            subscriber.cancel()
        }

    @Test
    fun `onStartInspection does nothing for unknown inspection id`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onStartInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            subscriber.cancel()
        }

    @Test
    fun `onEndInspection does nothing for unknown inspection id`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onEndInspection(Uuid.random())
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            subscriber.cancel()
        }

    @Test
    fun `onStartInspection syncs the inspection`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onStartInspection(inspectionId)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            val apiResult = fakeInspectionsApi.getInspections(projectId)
            assertIs<OnlineDataResult.Success<List<AppInspection>>>(apiResult)
            val synced = apiResult.data.find { it.id == inspectionId }
            assertEquals(fixedNow, synced?.dateFrom)
            subscriber.cancel()
        }

    @Test
    fun `onEndInspection syncs the inspection`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val inspectionId = Uuid.random()
            seedProject(projectId)
            seedInspection(projectId = projectId, inspectionId = inspectionId, dateFrom = Timestamp(1L))

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onEndInspection(inspectionId)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            val apiResult = fakeInspectionsApi.getInspections(projectId)
            assertIs<OnlineDataResult.Success<List<AppInspection>>>(apiResult)
            val synced = apiResult.data.find { it.id == inspectionId }
            assertEquals(fixedNow, synced?.dateTo)
            subscriber.cancel()
        }

    private fun seedClosedProject(projectId: Uuid): AppProject {
        seedCurrentUser()
        val project =
            AppProjectData(
                id = projectId,
                name = "Closed Project",
                address = "Test Address",
                creatorId = UuidProvider.getUuid(),
                isClosed = true,
                updatedAt = Timestamp(10L),
            )
        fakeProjectsDb.setProject(project)
        fakeProjectAssignmentsDb.setAssignments(projectId, setOf(currentUserId))
        return project
    }

    @Test
    fun `onToggleClose on open project sets isClosed to true`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isClosed)

            viewModel.onToggleClose()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isClosed)
            subscriber.cancel()
        }

    @Test
    fun `onToggleClose on closed project sets isClosed to false`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedClosedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isClosed)

            viewModel.onToggleClose()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isClosed)
            subscriber.cancel()
        }

    @Test
    fun `onToggleClose network failure sends error`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()
            seedProject(projectId)
            val fakeProjectsApi: FakeProjectsApi by inject()
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onToggleClose()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.NetworkUnavailable>(error)
            subscriber.cancel()
        }

    @Test
    fun `canToggleClosed is false when not loaded`() =
        runTest(testDispatcher) {
            val projectId = UuidProvider.getUuid()

            val viewModel = createViewModel(projectId)

            assertFalse(viewModel.state.value.canToggleClosed)
        }
}
