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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakePullSyncTimestampDb
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.lib.sync.fe.ports.PullSyncTimestampDb
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncQueue
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
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
    private val fakePullSyncTimestampDb: FakePullSyncTimestampDb by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val useCase: PullProjectChangesUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeSyncQueue) bind SyncQueue::class
                singleOf(::FakePullSyncTimestampDb) bind PullSyncTimestampDb::class
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
            },
        )

    private fun createProject(id: Uuid) =
        FrontendProject(
            project =
                Project(
                    id = id,
                    name = "Test Project",
                    address = "Test Address",
                    updatedAt = Timestamp(100L),
                ),
        )

    private fun createStructure(
        id: Uuid,
        projectId: Uuid,
    ) = FrontendStructure(
        structure =
            Structure(
                id = id,
                projectId = projectId,
                name = "Structure",
                floorPlanUrl = null,
                updatedAt = Timestamp(1L),
            ),
    )

    @Test
    fun `upserts alive projects to db`() =
        runTest(testDispatcher) {
            val project = createProject(projectId)
            fakeProjectsApi.modifiedSinceResults =
                listOf(
                    ProjectSyncResult.Updated(project = project),
                )

            useCase()

            val result = fakeProjectsDb.getProjectFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(result)
            assertNotNull(result.data)
            assertEquals(projectId, result.data!!.project.id)
        }

    @Test
    fun `deletes soft-deleted projects and cascades`() =
        runTest(testDispatcher) {
            val project = createProject(projectId)
            fakeProjectsDb.setProject(project)

            val structure = createStructure(id = structureId, projectId = projectId)
            fakeStructuresDb.setStructure(structure)

            fakeProjectsApi.modifiedSinceResults =
                listOf(
                    ProjectSyncResult.Deleted(id = projectId),
                )

            useCase()

            val structuresResult = fakeStructuresDb.getStructuresFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<FrontendStructure>>>(structuresResult)
            assertTrue(structuresResult.data.isEmpty())

            val result = fakeProjectsDb.getProjectFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(result)
            assertNull(result.data)
        }

    @Test
    fun `stores last synced timestamp on success`() =
        runTest(testDispatcher) {
            fakeProjectsApi.modifiedSinceResults = emptyList()

            useCase()

            assertNotNull(fakePullSyncTimestampDb.getLastSyncedAt(PROJECT_SYNC_ENTITY_TYPE, ""))
        }

    @Test
    fun `does not store timestamp on API failure`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase()

            assertNull(fakePullSyncTimestampDb.getLastSyncedAt(PROJECT_SYNC_ENTITY_TYPE, ""))
        }
}
