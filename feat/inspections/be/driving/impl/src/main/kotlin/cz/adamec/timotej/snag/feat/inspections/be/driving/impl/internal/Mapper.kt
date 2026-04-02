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

package cz.adamec.timotej.snag.feat.inspections.be.driving.impl.internal

import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.feat.inspections.contract.InspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.contract.PutInspectionApiDto
import kotlin.uuid.Uuid

internal fun BackendInspection.toDto() =
    InspectionApiDto(
        id = id,
        projectId = projectId,
        startedAt = startedAt,
        endedAt = endedAt,
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun PutInspectionApiDto.toModel(id: Uuid) =
    BackendInspectionData(
        id = id,
        projectId = projectId,
        startedAt = startedAt,
        endedAt = endedAt,
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = updatedAt,
    )
