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

import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.InspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.be.driving.contract.PutInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection

internal fun InspectionApiDto.toModel() =
    FrontendInspection(
        inspection =
            Inspection(
                id = id,
                projectId = projectId,
                startedAt = startedAt,
                endedAt = endedAt,
                participants = participants,
                climate = climate,
                note = note,
                updatedAt = updatedAt,
            ),
    )

internal fun FrontendInspection.toPutApiDto() =
    PutInspectionApiDto(
        projectId = inspection.projectId,
        startedAt = inspection.startedAt,
        endedAt = inspection.endedAt,
        participants = inspection.participants,
        climate = inspection.climate,
        note = inspection.note,
        updatedAt = inspection.updatedAt,
    )
