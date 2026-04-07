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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.api

import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.contract.InspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.contract.PutInspectionApiDto

internal fun InspectionApiDto.toModel() =
    AppInspectionData(
        id = id,
        projectId = projectId,
        dateFrom = dateFrom,
        dateTo = dateTo,
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = updatedAt,
    )

internal fun AppInspection.toPutApiDto() =
    PutInspectionApiDto(
        projectId = projectId,
        dateFrom = dateFrom,
        dateTo = dateTo,
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = updatedAt,
    )
