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
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.NonWebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class NonWebAddFindingPhotoUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: NonWebAddFindingPhotoUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val photoBytes = byteArrayOf(1, 2, 3, 4)

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                factory { FakeLocalFileStorage() } bind LocalFileStorage::class
            },
        )

    @Test
    fun `saves photo to database and returns photo id`() =
        runTest(testDispatcher) {
            val request =
                AddFindingPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    findingId = findingId,
                    projectId = projectId,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedPhoto = getSavedPhoto(result.data)
            assertEquals(result.data, savedPhoto.id)
            assertEquals(findingId, savedPhoto.findingId)
        }

    @Test
    fun `enqueues sync save operation`() =
        runTest(testDispatcher) {
            val request =
                AddFindingPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    findingId = findingId,
                    projectId = projectId,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(FINDING_PHOTO_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(result.data, pending[0].entityId)
            assertEquals(SyncOperationType.UPSERT, pending[0].operationType)
        }

    @Test
    fun `saved photo URL contains project and finding path`() =
        runTest(testDispatcher) {
            val request =
                AddFindingPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    findingId = findingId,
                    projectId = projectId,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedPhoto = getSavedPhoto(result.data)
            assertTrue(savedPhoto.url.contains(projectId.toString()))
            assertTrue(savedPhoto.url.contains(findingId.toString()))
        }

    @Test
    fun `extracts file extension from fileName`() =
        runTest(testDispatcher) {
            val request =
                AddFindingPhotoRequest(
                    bytes = photoBytes,
                    fileName = "photo.jpg",
                    findingId = findingId,
                    projectId = projectId,
                )

            val result = useCase(request)

            assertIs<OfflineFirstDataResult.Success<Uuid>>(result)
            val savedPhoto = getSavedPhoto(result.data)
            assertTrue(savedPhoto.url.endsWith(".jpg"))
        }

    private suspend fun getSavedPhoto(id: Uuid): AppFindingPhoto {
        fakeFindingPhotosDb.forcedFailure = null
        val result = fakeFindingPhotosDb.getPhotoFlow(id).first()
        return (result as OfflineFirstDataResult.Success).data!!
    }
}

private class FakeLocalFileStorage : LocalFileStorage {
    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        subdirectory: String,
    ): String = "$subdirectory/$fileName"

    override suspend fun readFileBytes(path: String): ByteArray = byteArrayOf()

    override suspend fun deleteFile(path: String) = Unit
}
