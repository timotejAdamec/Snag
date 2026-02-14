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

import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.shared.database.be.InspectionEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal fun InspectionEntity.toModel() =
    BackendInspection(
        inspection =
            Inspection(
                id = id.value,
                projectId = project.id.value,
                startedAt = startedAt?.let { Timestamp(it) },
                endedAt = endedAt?.let { Timestamp(it) },
                participants = participants,
                climate = climate,
                note = note,
                updatedAt = Timestamp(updatedAt),
            ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
