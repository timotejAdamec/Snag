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

package cz.adamec.timotej.snag.structures.be.driving.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.structures.contract.PutStructureApiDto
import cz.adamec.timotej.snag.structures.contract.StructureApiDto
import kotlin.uuid.Uuid

internal fun BackendStructure.toDto() =
    StructureApiDto(
        id = id,
        projectId = projectId,
        name = name,
        floorPlanUrl = floorPlanUrl,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun PutStructureApiDto.toModel(id: Uuid) =
    BackendStructureData(
        id = id,
        projectId = projectId,
        name = name,
        floorPlanUrl = floorPlanUrl,
        updatedAt = updatedAt,
    )
