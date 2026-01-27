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

import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntity
import cz.adamec.timotej.snag.feat.structures.business.Structure
import kotlin.uuid.Uuid

internal fun Structure.toEntity() =
    StructureEntity(
        id = id.toString(),
        projectId = projectId.toString(),
        name = name,
        floorPlanUrl = floorPlanUrl,
    )

internal fun StructureEntity.toBusiness() =
    Structure(
        id = Uuid.parse(id),
        projectId = Uuid.parse(projectId),
        name = name,
        floorPlanUrl = floorPlanUrl,
    )
