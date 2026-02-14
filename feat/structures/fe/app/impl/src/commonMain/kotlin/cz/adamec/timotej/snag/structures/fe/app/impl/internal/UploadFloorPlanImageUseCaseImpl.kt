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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.UploadFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresFileStorage
import kotlin.uuid.Uuid

class UploadFloorPlanImageUseCaseImpl(
    private val structuresFileStorage: StructuresFileStorage,
) : UploadFloorPlanImageUseCase {
    override suspend fun invoke(
        projectId: Uuid,
        structureId: Uuid,
        bytes: ByteArray,
        fileName: String,
    ): OnlineDataResult<String> =
        structuresFileStorage.uploadFile(
            bytes = bytes,
            fileName = fileName,
            directory = "projects/$projectId/structures/$structureId",
        )
}
