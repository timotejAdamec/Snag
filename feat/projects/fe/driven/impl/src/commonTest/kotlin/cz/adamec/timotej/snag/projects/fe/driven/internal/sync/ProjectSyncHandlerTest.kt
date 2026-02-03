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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProjectSyncHandlerTest {
    private lateinit var projectsApi: FakeProjectsApi
    private lateinit var projectsDb: FakeProjectsDb
    private lateinit var handler: ProjectSyncHandler

    @BeforeTest
    fun setUp() {
        projectsApi = FakeProjectsApi()
        projectsDb = FakeProjectsDb()
        handler = ProjectSyncHandler(projectsApi, projectsDb)
    }

    @Test
    fun `upsert reads from db and calls api`() =
        runTest {
            val project = Project(Uuid.random(), "Test Project", "123 Street")
            projectsDb.setProject(project)

            val result = handler.execute(project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `upsert saves fresher dto from api to db`() =
        runTest {
            val project = Project(Uuid.random(), "Original", "123 Street")
            projectsDb.setProject(project)

            val fresherProject = project.copy(name = "Updated by API")
            projectsApi.saveProjectResponseOverride = { OnlineDataResult.Success(fresherProject) }

            val result = handler.execute(project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            val dbResult = projectsDb.getProjectFlow(project.id).first()
            val savedProject = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals("Updated by API", savedProject?.name)
        }

    @Test
    fun `upsert when entity not in db returns entity not found`() =
        runTest {
            val result = handler.execute(Uuid.random(), SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `upsert when api fails returns failure`() =
        runTest {
            val project = Project(Uuid.random(), "Test Project", "123 Street")
            projectsDb.setProject(project)
            projectsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(project.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Failure, result)
        }

    @Test
    fun `delete calls api and returns success`() =
        runTest {
            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
        }

    @Test
    fun `delete when api fails returns failure`() =
        runTest {
            projectsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            val result = handler.execute(Uuid.random(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Failure, result)
        }
}
