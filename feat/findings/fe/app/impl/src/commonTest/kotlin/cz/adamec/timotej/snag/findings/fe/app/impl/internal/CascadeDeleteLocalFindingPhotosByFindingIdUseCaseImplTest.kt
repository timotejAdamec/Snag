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
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeDeleteLocalFindingPhotosByFindingIdUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeDeleteLocalFindingPhotosByFindingIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()

    private val useCase: CascadeDeleteLocalFindingPhotosByFindingIdUseCase by inject()

    private val findingId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")

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
    fun `deletes all photos for the given finding`() =
        runTest(testDispatcher) {
            val photo1 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    findingId = findingId1,
                )
            val photo2 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    findingId = findingId1,
                )
            fakeFindingPhotosDb.setPhotos(listOf(photo1, photo2))

            useCase(findingId1)

            val result = fakeFindingPhotosDb.getPhotosFlow(findingId1).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(result)
            assertTrue(result.data.isEmpty())
        }

    @Test
    fun `does not delete photos from other findings`() =
        runTest(testDispatcher) {
            val photoForFinding1 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    findingId = findingId1,
                )
            val photoForFinding2 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    findingId = findingId2,
                )
            fakeFindingPhotosDb.setPhotos(listOf(photoForFinding1, photoForFinding2))

            useCase(findingId1)

            val result = fakeFindingPhotosDb.getPhotosFlow(findingId2).first()
            assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(result)
            assertTrue(result.data.size == 1)
        }
}
