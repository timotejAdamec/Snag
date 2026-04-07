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

package cz.adamec.timotej.snag.projects.be.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhotoData
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb
import kotlin.uuid.Uuid

val TEST_PROJECT_PHOTO_ID: Uuid = Uuid.parse("00000000-0000-0000-0002-000000000001")

suspend fun ProjectPhotosDb.seedTestProjectPhoto(
    id: Uuid = TEST_PROJECT_PHOTO_ID,
    projectId: Uuid = TEST_PROJECT_ID,
    url: String = "https://example.com/photo.jpg",
    description: String = "Test photo",
    updatedAt: Timestamp = Timestamp(1L),
    deletedAt: Timestamp? = null,
) {
    savePhoto(
        BackendProjectPhotoData(
            id = id,
            projectId = projectId,
            url = url,
            description = description,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        ),
    )
}
