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

package cz.adamec.timotej.snag.structures.fe.driven.internal.api

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.structures.be.driving.contract.PutStructureApiDto
import cz.adamec.timotej.snag.structures.be.driving.contract.StructureApiDto

internal fun StructureApiDto.toModel() =
    FrontendStructure(
        structure = Structure(
            id = id,
            projectId = projectId,
            name = name,
            floorPlanUrl = floorPlanUrl,
            updatedAt = updatedAt,
        )
    )

internal fun FrontendStructure.toPutApiDto() =
    PutStructureApiDto(
        projectId = structure.projectId,
        name = structure.name,
        floorPlanUrl = structure.floorPlanUrl,
        updatedAt = structure.updatedAt,
    )
