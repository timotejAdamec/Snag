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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeFileApi
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoRequest
import cz.adamec.timotej.snag.projects.fe.app.api.WebAddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class WebAddProjectPhotoUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val fakeFileApi: FakeFileApi by inject()

    private val useCase: WebAddProjectPhotoUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoBytes = byteArrayOf(1, 2, 3, 4)

    @Test
    fun `uploads to remote storage and saves photo to database`() =
        runTest(testDispatcher) {
            val request =
                AddProjectPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    projectId = projectId,
                    description = "Test description",
                )

            val result = useCase(request)

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            assertTrue(fakeFileApi.uploadedFiles.isNotEmpty())

            val savedPhoto = getSavedPhoto(result.data)
            assertEquals(result.data, savedPhoto.id)
            assertEquals(projectId, savedPhoto.projectId)
            assertEquals("Test description", savedPhoto.description)
            assertTrue(savedPhoto.url.startsWith("https://"))
        }

    @Test
    fun `enqueues sync save operation on success`() =
        runTest(testDispatcher) {
            val request =
                AddProjectPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    projectId = projectId,
                    description = "Test",
                )

            val result = useCase(request)

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(PROJECT_PHOTO_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(result.data, pending[0].entityId)
            assertEquals(SyncOperationType.UPSERT, pending[0].operationType)
        }

    @Test
    fun `returns failure when remote upload fails`() =
        runTest(testDispatcher) {
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("Upload failed"))

            val request =
                AddProjectPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    projectId = projectId,
                    description = "Test",
                )

            val result = useCase(request)

            assertIs<OnlineDataResult.Failure>(result)
            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }

    private suspend fun getSavedPhoto(id: Uuid): AppProjectPhoto {
        fakeProjectPhotosDb.forcedFailure = null
        val result = fakeProjectPhotosDb.getPhotoFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}
