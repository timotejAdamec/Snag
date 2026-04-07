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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()

    private val useCase: DeleteProjectUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createProject(id: Uuid) =
        AppProjectData(
            id = id,
            name = "Test Project",
            address = "Test Address",
            creatorId = UuidProvider.getUuid(),
            updatedAt = Timestamp(100L),
        )

    private fun createStructure(
        id: Uuid,
        projectId: Uuid,
    ) = AppStructureData(
        id = id,
        projectId = projectId,
        name = "Structure",
        floorPlanUrl = null,
        updatedAt = Timestamp(1L),
    )

    @Test
    fun `deletes project and cascade deletes structures`() =
        runTest(testDispatcher) {
            val project = createProject(projectId)
            fakeProjectsDb.setProject(project)

            val structure = createStructure(id = structureId, projectId = projectId)
            fakeStructuresDb.setStructure(structure)

            useCase(projectId)

            val projectResult = fakeProjectsDb.getProjectFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<AppProject?>>(projectResult)
            assertNull(projectResult.data)

            val structuresResult = fakeStructuresDb.getStructuresFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppStructure>>>(structuresResult)
            assertTrue(structuresResult.data.isEmpty())
        }

    @Test
    fun `enqueues sync delete on success`() =
        runTest(testDispatcher) {
            val project = createProject(projectId)
            fakeProjectsDb.setProject(project)

            useCase(projectId)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(PROJECT_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(projectId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
        }

    @Test
    fun `does not enqueue sync delete on failure`() =
        runTest(testDispatcher) {
            fakeProjectsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            useCase(projectId)

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }

    @Test
    fun `deletes project and cascade deletes project photos`() =
        runTest(testDispatcher) {
            val project = createProject(projectId)
            fakeProjectsDb.setProject(project)

            val photo =
                AppProjectPhotoData(
                    id = photoId,
                    projectId = projectId,
                    url = "https://example.com/photo.jpg",
                    description = "Photo",
                    updatedAt = Timestamp(1L),
                )
            fakeProjectPhotosDb.setPhoto(photo)

            useCase(projectId)

            val photosResult = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppProjectPhoto>>>(photosResult)
            assertTrue(photosResult.data.isEmpty())
        }
}
