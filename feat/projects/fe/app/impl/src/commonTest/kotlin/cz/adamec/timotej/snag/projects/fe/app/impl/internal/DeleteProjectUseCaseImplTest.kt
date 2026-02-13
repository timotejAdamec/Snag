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
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsSync
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteProjectUseCaseImplTest : FrontendKoinInitializedTest() {

    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeProjectsSync: FakeProjectsSync by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val useCase: DeleteProjectUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
                singleOf(::FakeStructuresPullSyncTimestampDataSource) bind StructuresPullSyncTimestampDataSource::class
                singleOf(::FakeFindingsDb) bind FindingsDb::class
            },
        )

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")

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
    fun `deletes project and cascade deletes structures`() = runTest(testDispatcher) {
        val project = createProject(projectId)
        fakeProjectsDb.setProject(project)

        val structure = createStructure(id = structureId, projectId = projectId)
        fakeStructuresDb.setStructure(structure)

        useCase(projectId)

        val projectResult = fakeProjectsDb.getProjectFlow(projectId).first()
        assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(projectResult)
        assertNull(projectResult.data)

        val structuresResult = fakeStructuresDb.getStructuresFlow(projectId).first()
        assertIs<OfflineFirstDataResult.Success<List<FrontendStructure>>>(structuresResult)
        assertTrue(structuresResult.data.isEmpty())
    }

    @Test
    fun `enqueues sync delete on success`() = runTest(testDispatcher) {
        val project = createProject(projectId)
        fakeProjectsDb.setProject(project)

        useCase(projectId)

        assertEquals(listOf(projectId), fakeProjectsSync.deletedProjectIds)
    }

    @Test
    fun `does not enqueue sync delete on failure`() = runTest(testDispatcher) {
        fakeProjectsDb.forcedFailure =
            OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

        useCase(projectId)

        assertTrue(fakeProjectsSync.deletedProjectIds.isEmpty())
    }
}
