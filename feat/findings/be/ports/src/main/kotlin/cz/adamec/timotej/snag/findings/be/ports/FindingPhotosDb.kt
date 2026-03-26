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

package cz.adamec.timotej.snag.findings.be.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import kotlin.uuid.Uuid

interface FindingPhotosDb {
    suspend fun savePhoto(photo: BackendFindingPhoto): BackendFindingPhoto?

    suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFindingPhoto?

    suspend fun getPhotosModifiedSince(
        findingId: Uuid,
        since: Timestamp,
    ): List<BackendFindingPhoto>
}
