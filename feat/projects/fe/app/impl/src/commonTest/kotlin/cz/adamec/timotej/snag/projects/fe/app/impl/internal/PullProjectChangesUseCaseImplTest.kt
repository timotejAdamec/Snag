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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsPullSyncCoordinator
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsSync
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncCoordinator
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeDeleteLocalStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.structures.fe.app.test.FakeCascadeDeleteLocalStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class PullProjectChangesUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakePullSyncTimestampDataSource: FakeProjectsPullSyncTimestampDataSource by inject()

    private val useCase: PullProjectChangesUseCase by inject()

    private val fakeCascadeDelete = FakeCascadeDeleteLocalStructuresByProjectIdUseCase()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
                singleOf(::FakeProjectsPullSyncTimestampDataSource) bind ProjectsPullSyncTimestampDataSource::class
                singleOf(::FakeProjectsPullSyncCoordinator) bind ProjectsPullSyncCoordinator::class
                single { fakeCascadeDelete } bind CascadeDeleteLocalStructuresByProjectIdUseCase::class
            },
        )

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private fun createProject(id: Uuid) = FrontendProject(
        project = Project(
            id = id,
            name = "Test Project",
            address = "Test Address",
            updatedAt = Timestamp(100L),
        ),
    )

    @Test
    fun `upserts alive projects to db`() = runTest(testDispatcher) {
        val project = createProject(projectId)
        fakeProjectsApi.modifiedSinceResults = listOf(
            ProjectSyncResult.Updated(project = project),
        )

        useCase()

        val result = fakeProjectsDb.getProjectFlow(projectId).first()
        assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(result)
        assertNotNull(result.data)
        assertEquals(projectId, result.data!!.project.id)
    }

    @Test
    fun `deletes soft-deleted projects and cascades`() = runTest(testDispatcher) {
        val project = createProject(projectId)
        fakeProjectsDb.setProject(project)

        fakeProjectsApi.modifiedSinceResults = listOf(
            ProjectSyncResult.Deleted(id = projectId),
        )

        useCase()

        assertTrue(fakeCascadeDelete.deletedProjectIds.contains(projectId))

        val result = fakeProjectsDb.getProjectFlow(projectId).first()
        assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(result)
        assertNull(result.data)
    }

    @Test
    fun `stores last synced timestamp on success`() = runTest(testDispatcher) {
        fakeProjectsApi.modifiedSinceResults = emptyList()

        useCase()

        assertNotNull(fakePullSyncTimestampDataSource.getLastSyncedAt())
    }

    @Test
    fun `does not store timestamp on API failure`() = runTest(testDispatcher) {
        fakeProjectsApi.forcedFailure =
            OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

        useCase()

        assertNull(fakePullSyncTimestampDataSource.getLastSyncedAt())
    }
}
