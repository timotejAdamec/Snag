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

import app.cash.turbine.test
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetFindingPhotosUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()

    private val useCase: GetFindingPhotosUseCase by inject()

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
    fun `returns photos for the given finding`() =
        runTest(testDispatcher) {
            val photo1 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    findingId = findingId1,
                )
            val photo2 =
                createPhoto(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    findingId = findingId2,
                )
            fakeFindingPhotosDb.setPhotos(listOf(photo1, photo2))

            useCase(findingId1).test {
                val result = awaitItem()
                assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(result)
                assertEquals(1, result.data.size)
                assertEquals(photo1.id, result.data[0].id)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `returns empty list when no photos exist`() =
        runTest(testDispatcher) {
            val nonExistentFindingId = Uuid.parse("00000000-0000-0000-0099-000000000099")

            useCase(nonExistentFindingId).test {
                val result = awaitItem()
                assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(result)
                assertTrue(result.data.isEmpty())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `emits updated list when photo is added`() =
        runTest(testDispatcher) {
            useCase(findingId1).test {
                val initial = awaitItem()
                assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(initial)
                assertTrue(initial.data.isEmpty())

                val newPhoto =
                    createPhoto(
                        id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                        findingId = findingId1,
                    )
                fakeFindingPhotosDb.setPhoto(newPhoto)

                val updated = awaitItem()
                assertIs<OfflineFirstDataResult.Success<List<AppFindingPhoto>>>(updated)
                assertEquals(1, updated.data.size)
                assertEquals(newPhoto.id, updated.data[0].id)

                cancelAndConsumeRemainingEvents()
            }
        }
}
