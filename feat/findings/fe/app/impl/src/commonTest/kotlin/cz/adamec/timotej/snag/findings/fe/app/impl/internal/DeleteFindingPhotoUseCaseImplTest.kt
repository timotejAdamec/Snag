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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
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
import kotlin.uuid.Uuid

class DeleteFindingPhotoUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: DeleteFindingPhotoUseCase by inject()

    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0001-000000000001")

    private fun createPhoto(
        id: Uuid,
        findingId: Uuid,
    ) = AppFindingPhotoData(
        id = id,
        findingId = findingId,
        url = "https://storage.test/photo.jpg",
        createdAt = Timestamp(1L),
    )

    @Test
    fun `keeps photo in database for sync handler to delete`() =
        runTest(testDispatcher) {
            val photo = createPhoto(id = photoId, findingId = findingId)
            fakeFindingPhotosDb.setPhoto(photo)

            useCase(photoId)

            val flowResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
            assertIs<OfflineFirstDataResult.Success<AppFindingPhoto?>>(flowResult)
            assertEquals(photo, flowResult.data)
        }

    @Test
    fun `enqueues sync delete operation`() =
        runTest(testDispatcher) {
            val photo = createPhoto(id = photoId, findingId = findingId)
            fakeFindingPhotosDb.setPhoto(photo)

            useCase(photoId)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(FINDING_PHOTO_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(photoId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
        }

    @Test
    fun `returns success when photo exists`() =
        runTest(testDispatcher) {
            val photo = createPhoto(id = photoId, findingId = findingId)
            fakeFindingPhotosDb.setPhoto(photo)

            val result = useCase(photoId)

            assertIs<OfflineFirstDataResult.Success<Unit>>(result)
        }

    @Test
    fun `returns success even when photo does not exist`() =
        runTest(testDispatcher) {
            val nonExistentId = Uuid.parse("00000000-0000-0000-0099-000000000099")

            val result = useCase(nonExistentId)

            assertIs<OfflineFirstDataResult.Success<Unit>>(result)
        }
}
