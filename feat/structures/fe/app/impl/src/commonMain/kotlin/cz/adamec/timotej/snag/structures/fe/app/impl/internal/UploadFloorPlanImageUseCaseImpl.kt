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
import cz.adamec.timotej.snag.lib.core.fe.onFailure
import cz.adamec.timotej.snag.lib.core.fe.onSuccess
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
    ): OnlineDataResult<String> {
        LH.logger.d {
            "Uploading floor plan image $fileName for structure $structureId in project $projectId."
        }
        return structuresFileStorage.uploadFile(
            bytes = bytes,
            fileName = fileName,
            directory = "projects/$projectId/structures/$structureId",
        ).onSuccess {
            LH.logger.d {
                "Uploaded floor plan image $fileName for structure $structureId in project $projectId." +
                        " The floor plan is now available at $it."
            }
        }.onFailure {
            LH.logger.w {
                "Error uploading floor plan image $fileName for structure $structureId in project $projectId."
            }
        }
    }
}
