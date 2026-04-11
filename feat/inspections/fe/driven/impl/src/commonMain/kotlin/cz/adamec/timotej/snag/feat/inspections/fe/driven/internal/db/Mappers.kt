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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.InspectionEntity
import kotlin.uuid.Uuid

internal fun AppInspection.toEntity() =
    InspectionEntity(
        id = id.toString(),
        projectId = projectId.toString(),
        dateFrom = dateFrom?.value,
        dateTo = dateTo?.value,
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = updatedAt.value,
    )

internal fun InspectionEntity.toModel() =
    AppInspectionData(
        id = Uuid.parse(id),
        projectId = Uuid.parse(projectId),
        dateFrom = dateFrom?.let { Timestamp(it) },
        dateTo = dateTo?.let { Timestamp(it) },
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = Timestamp(updatedAt),
    )
