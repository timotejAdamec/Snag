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

package cz.adamec.timotej.snag.structures.fe.driven.internal.db

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.StructureEntity
import kotlin.uuid.Uuid

internal fun AppStructure.toEntity() =
    StructureEntity(
        id = id.toString(),
        projectId = projectId.toString(),
        name = name,
        floorPlanUrl = floorPlanUrl,
        updatedAt = updatedAt.value,
    )

internal fun StructureEntity.toModel() =
    AppStructureData(
        id = Uuid.parse(id),
        projectId = Uuid.parse(projectId),
        name = name,
        floorPlanUrl = floorPlanUrl,
        updatedAt = Timestamp(updatedAt),
    )
