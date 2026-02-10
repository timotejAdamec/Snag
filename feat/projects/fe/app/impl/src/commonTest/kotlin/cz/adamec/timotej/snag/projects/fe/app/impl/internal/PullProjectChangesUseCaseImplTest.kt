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
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
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
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
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
    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val useCase: PullProjectChangesUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
                singleOf(::FakeProjectsPullSyncTimestampDataSource) bind ProjectsPullSyncTimestampDataSource::class
                singleOf(::FakeProjectsPullSyncCoordinator) bind ProjectsPullSyncCoordinator::class
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    private fun createProject(id: Uuid) = FrontendProject(
        project = Project(
            id = id,
            name = "Test Project",
            address = "Test Address",
            updatedAt = Timestamp(100L),
        ),
    )

    private fun createStructure(id: Uuid, projectId: Uuid) = FrontendStructure(
        structure = Structure(
            id = id,
            projectId = projectId,
            name = "Structure",
            floorPlanUrl = null,
            updatedAt = Timestamp(1L),
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

        val structure = createStructure(id = structureId, projectId = projectId)
        fakeStructuresDb.setStructure(structure)

        fakeProjectsApi.modifiedSinceResults = listOf(
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
