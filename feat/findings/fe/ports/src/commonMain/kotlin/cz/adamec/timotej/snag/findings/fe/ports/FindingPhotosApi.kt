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

package cz.adamec.timotej.snag.findings.fe.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import kotlin.uuid.Uuid

sealed interface FindingPhotoSyncResult {
    data class Deleted(val id: Uuid) : FindingPhotoSyncResult

    data class Updated(val photo: AppFindingPhoto) : FindingPhotoSyncResult
}

interface FindingPhotosApi {
    suspend fun savePhoto(photo: AppFindingPhoto): OnlineDataResult<AppFindingPhoto?>

    suspend fun deletePhoto(
        id: Uuid,
        findingId: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppFindingPhoto?>

    suspend fun getPhotosModifiedSince(
        findingId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingPhotoSyncResult>>
}
