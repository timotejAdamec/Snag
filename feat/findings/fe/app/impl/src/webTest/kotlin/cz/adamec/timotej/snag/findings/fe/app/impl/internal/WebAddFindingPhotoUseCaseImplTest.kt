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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.WebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeFileApi
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class WebAddFindingPhotoUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val fakeFileApi: FakeFileApi by inject()

    private val useCase: WebAddFindingPhotoUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val photoBytes = byteArrayOf(1, 2, 3, 4)

    private fun createRequest(fileName: String = "photo.jpg") =
        AddFindingPhotoRequest(
            bytes = photoBytes,
            fileName = fileName,
            findingId = findingId,
            projectId = projectId,
        )

    @Test
    fun `uploads photo to remote and saves to database`() =
        runTest(testDispatcher) {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val photoId = result.data
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data
            assertTrue(
                savedPhoto!!.url.startsWith("https://"),
                "Saved photo URL should be remote, was: ${savedPhoto.url}",
            )
            assertEquals(findingId, savedPhoto.findingId)
        }

    @Test
    fun `returns photo id on success`() =
        runTest(testDispatcher) {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val photoId = result.data
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals(photoId, savedPhoto!!.id)
        }

    @Test
    fun `returns failure when upload fails`() =
        runTest(testDispatcher) {
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("Upload failed"))

            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Failure>(result)
        }

    @Test
    fun `enqueues sync save operation`() =
        runTest(testDispatcher) {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(FINDING_PHOTO_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(result.data, pending[0].entityId)
            assertEquals(SyncOperationType.UPSERT, pending[0].operationType)
        }

    @Test
    fun `repeated invocations create separate photos`() =
        runTest(testDispatcher) {
            val result1 = useCase(createRequest())
            val result2 = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result1)
            assertIs<OnlineDataResult.Success<Uuid>>(result2)
            assertNotEquals(
                result1.data,
                result2.data,
                "Each invocation should produce a unique photo ID",
            )
            val pending = fakeSyncQueue.getAllPending()
            assertEquals(2, pending.size)
        }

    @Test
    fun `saved photo URL contains project and finding path`() =
        runTest(testDispatcher) {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(result.data).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data!!
            assertTrue(
                savedPhoto.url.contains(projectId.toString()),
                "URL should contain project ID",
            )
            assertTrue(
                savedPhoto.url.contains(findingId.toString()),
                "URL should contain finding ID",
            )
        }

    @Test
    fun `extracts file extension from fileName`() =
        runTest(testDispatcher) {
            val result = useCase(createRequest(fileName = "image.png"))

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(result.data).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data!!
            assertTrue(
                savedPhoto.url.endsWith(".png"),
                "URL should end with .png extension, was: ${savedPhoto.url}",
            )
        }

    @Test
    fun `emits progress callback with start and end values`() =
        runTest(testDispatcher) {
            val progressEvents = mutableListOf<Float>()

            val result =
                useCase(
                    request = createRequest(),
                    onProgress = { progressEvents.add(it) },
                )

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            assertEquals(
                listOf(0f, 1f),
                progressEvents,
                "Progress should emit 0f before upload and 1f after success",
            )
        }
}
