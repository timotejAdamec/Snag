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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeFindingPhotosDb : FindingPhotosDb {
    private val ops = FakeDbOps<AppFindingPhoto>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getPhotosFlow(findingId: Uuid): Flow<OfflineFirstDataResult<List<AppFindingPhoto>>> =
        ops.allItemsFlow { it.findingId == findingId }

    override fun getPhotoFlow(id: Uuid): Flow<OfflineFirstDataResult<AppFindingPhoto?>> = ops.itemByIdFlow(id)

    override suspend fun savePhoto(photo: AppFindingPhoto): OfflineFirstDataResult<Unit> = ops.saveOneItem(photo)

    override suspend fun deletePhoto(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    fun setPhoto(photo: AppFindingPhoto) = ops.setItem(photo)

    fun setPhotos(photos: List<AppFindingPhoto>) = ops.setItems(photos)
}
