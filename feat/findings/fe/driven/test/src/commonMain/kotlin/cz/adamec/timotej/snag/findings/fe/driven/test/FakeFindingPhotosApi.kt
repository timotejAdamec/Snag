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

package cz.adamec.timotej.snag.findings.fe.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosApi
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import kotlin.uuid.Uuid

class FakeFindingPhotosApi : FindingPhotosApi {
    private val ops =
        FakeApiOps<AppFindingPhoto, FindingPhotoSyncResult>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var savePhotoResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun savePhoto(photo: AppFindingPhoto): OnlineDataResult<AppFindingPhoto?> = ops.saveItem(photo)

    override suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppFindingPhoto?> = ops.deleteItemById(id)

    override suspend fun getPhotosModifiedSince(
        findingId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingPhotoSyncResult>> = ops.getModifiedSinceItems()

    fun setPhoto(photo: AppFindingPhoto) = ops.setItem(photo)

    fun setPhotos(photos: List<AppFindingPhoto>) = ops.setItems(photos)
}
