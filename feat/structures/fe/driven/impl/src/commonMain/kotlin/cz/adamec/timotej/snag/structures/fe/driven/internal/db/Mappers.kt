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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntity
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal fun FrontendStructure.toEntity() =
    StructureEntity(
        id = structure.id.toString(),
        projectId = structure.projectId.toString(),
        name = structure.name,
        floorPlanUrl = structure.floorPlanUrl,
        updatedAt = structure.updatedAt.value,
    )

internal fun StructureEntity.toModel() =
    FrontendStructure(
        structure = Structure(
            id = Uuid.parse(id),
            projectId = Uuid.parse(projectId),
            name = name,
            floorPlanUrl = floorPlanUrl,
            updatedAt = Timestamp(updatedAt),
        )
    )
