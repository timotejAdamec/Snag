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

package cz.adamec.timotej.snag.feat.inspections.be.driven.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.featShared.database.be.driven.api.InspectionEntity

internal fun InspectionEntity.toModel() =
    BackendInspectionData(
        id = id.value,
        projectId = project.id.value,
        dateFrom = dateFrom?.let { Timestamp(it) },
        dateTo = dateTo?.let { Timestamp(it) },
        participants = participants,
        climate = climate,
        note = note,
        updatedAt = Timestamp(updatedAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
