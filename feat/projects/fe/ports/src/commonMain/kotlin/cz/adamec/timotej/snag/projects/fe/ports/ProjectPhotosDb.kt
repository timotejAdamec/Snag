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

package cz.adamec.timotej.snag.projects.fe.ports

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ProjectPhotosDb {
    fun getPhotosFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppProjectPhoto>>>

    fun getPhotoFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProjectPhoto?>>

    suspend fun savePhoto(photo: AppProjectPhoto): OfflineFirstDataResult<Unit>

    suspend fun deletePhoto(id: Uuid): OfflineFirstDataResult<Unit>

    suspend fun deletePhotosByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit>
}
