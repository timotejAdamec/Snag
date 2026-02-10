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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.shared.database.fe.db.InspectionEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal fun FrontendInspection.toEntity() =
    InspectionEntity(
        id = inspection.id.toString(),
        projectId = inspection.projectId.toString(),
        startedAt = inspection.startedAt?.value,
        endedAt = inspection.endedAt?.value,
        participants = inspection.participants,
        climate = inspection.climate,
        note = inspection.note,
        updatedAt = inspection.updatedAt.value,
    )

internal fun InspectionEntity.toModel() =
    FrontendInspection(
        inspection =
            Inspection(
                id = Uuid.parse(id),
                projectId = Uuid.parse(projectId),
                startedAt = startedAt?.let { Timestamp(it) },
                endedAt = endedAt?.let { Timestamp(it) },
                participants = participants,
                climate = climate,
                note = note,
                updatedAt = Timestamp(updatedAt),
            ),
    )
