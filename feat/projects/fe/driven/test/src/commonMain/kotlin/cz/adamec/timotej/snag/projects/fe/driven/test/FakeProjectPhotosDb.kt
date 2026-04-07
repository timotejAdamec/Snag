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

package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeProjectPhotosDb : ProjectPhotosDb {
    private val ops = FakeDbOps<AppProjectPhoto>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getPhotosFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppProjectPhoto>>> =
        ops.allItemsFlow { it.projectId == projectId }

    override fun getPhotoFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProjectPhoto?>> = ops.itemByIdFlow(id)

    override suspend fun savePhoto(photo: AppProjectPhoto): OfflineFirstDataResult<Unit> = ops.saveOneItem(photo)

    override suspend fun deletePhoto(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    override suspend fun deletePhotosByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItemsWhere { it.projectId != projectId }

    fun setPhoto(photo: AppProjectPhoto) = ops.setItem(photo)

    fun setPhotos(photos: List<AppProjectPhoto>) = ops.setItems(photos)
}
