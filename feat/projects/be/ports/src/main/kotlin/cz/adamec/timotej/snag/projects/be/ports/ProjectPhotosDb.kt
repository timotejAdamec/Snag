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

package cz.adamec.timotej.snag.projects.be.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import kotlin.uuid.Uuid

interface ProjectPhotosDb {
    suspend fun savePhoto(photo: BackendProjectPhoto): BackendProjectPhoto?

    suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProjectPhoto?

    suspend fun getPhotosModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendProjectPhoto>
}
