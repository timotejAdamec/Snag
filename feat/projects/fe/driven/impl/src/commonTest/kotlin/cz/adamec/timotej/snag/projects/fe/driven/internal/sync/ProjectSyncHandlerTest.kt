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

package cz.adamec.timotej.snag.projects.fe.driven.internal.sync

import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
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
import kotlin.uuid.Uuid

class ProjectSyncHandlerTest : FrontendKoinInitializedTest() {

    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()

    private val handler: ProjectSyncHandler by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::ProjectSyncHandler)
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    @Test
    fun `upsert reads from db and calls api`() =
        runTest(testDispatcher) {
            val project = FrontendProject(project = Project(id = Uuid.random(), name = "Test Project", address = "123 Street", updatedAt = Timestamp(10L)))
            fakeProjectsDb.setProject(project)

            val result = handler.execute(project.project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `upsert saves fresher dto from api to db`() =
        runTest(testDispatcher) {
            val project = FrontendProject(project = Project(id = Uuid.random(), name = "Original", address = "123 Street", updatedAt = Timestamp(10L)))
            fakeProjectsDb.setProject(project)

            val fresherProject = project.copy(project = project.project.copy(name = "Updated by API"))
            fakeProjectsApi.saveProjectResponseOverride = { OnlineDataResult.Success(fresherProject) }

            val result = handler.execute(project.project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            val dbResult = fakeProjectsDb.getProjectFlow(project.project.id).first()
            val savedProject = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals("Updated by API", savedProject?.project?.name)
        }

    @Test
    fun `upsert when entity not in db returns entity not found`() =
        runTest(testDispatcher) {
            val result = handler.execute(Uuid.random(), SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `upsert when api fails returns failure`() =
        runTest(testDispatcher) {
            val project = FrontendProject(project = Project(id = Uuid.random(), name = "Test Project", address = "123 Street", updatedAt = Timestamp(10L)))
            fakeProjectsDb.setProject(project)
            fakeProjectsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(project.project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Failure, result)
        }

    @Test
    fun `delete calls api and returns success`() =
        runTest(testDispatcher) {
            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `delete when api fails returns failure`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Failure, result)
        }
}
