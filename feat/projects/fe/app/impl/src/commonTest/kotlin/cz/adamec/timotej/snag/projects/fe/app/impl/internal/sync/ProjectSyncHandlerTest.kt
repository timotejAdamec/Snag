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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.mp.KoinPlatform.getKoin
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProjectSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()

    private val handler: PushSyncOperationHandler by lazy {
        getKoin()
            .getAll<PushSyncOperationHandler>()
            .first { it.entityTypeId == PROJECT_SYNC_ENTITY_TYPE }
    }

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createProject(id: Uuid = projectId) =
        AppProjectData(
            id = id,
            name = "Test Project",
            address = "Test Address",
            creatorId = UuidProvider.getUuid(),
            updatedAt = Timestamp(10L),
        )

    @Test
    fun `upsert saves project to API`() =
        runTest(testDispatcher) {
            val project = createProject()
            fakeProjectsDb.setProject(project)

            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `upsert returns entity not found when project missing from DB`() =
        runTest(testDispatcher) {
            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `upsert returns failure when API fails`() =
        runTest(testDispatcher) {
            val project = createProject()
            fakeProjectsDb.setProject(project)
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `delete calls API delete`() =
        runTest(testDispatcher) {
            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.DELETE,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `delete returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.DELETE,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `on delete rejected cascade restores project photos`() =
        runTest(testDispatcher) {
            // Seed a photo that will be cascade-cleared during restore
            val photo =
                AppProjectPhotoData(
                    id = photoId,
                    projectId = projectId,
                    url = "https://example.com/photo.jpg",
                    description = "Photo",
                    updatedAt = Timestamp(1L),
                )
            fakeProjectPhotosDb.setPhoto(photo)

            // Set up API to reject the delete by returning the project (non-null)
            val rejectedProject = createProject()
            fakeProjectsApi.deleteProjectResponseOverride = {
                OnlineDataResult.Success(rejectedProject)
            }

            val result =
                handler.execute(
                    entityId = projectId,
                    operationType = SyncOperationType.DELETE,
                )

            assertEquals(PushSyncOperationResult.Success, result)

            // Project should be saved back to DB
            val projectResult = fakeProjectsDb.getProjectFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<AppProject?>>(projectResult)
            assertEquals(rejectedProject, projectResult.data)

            // Photos should have been cascade-cleared (restore clears + triggers pull sync)
            val photosResult = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<*>>(photosResult)
            assertTrue((photosResult.data as List<*>).isEmpty())
        }
}
