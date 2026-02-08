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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal fun FindingEntity.toModel() =
    BackendFinding(
        finding = Finding(
            id = id.value,
            structureId = structureId,
            name = name,
            description = description,
            coordinates =
                coordinates.map {
                    RelativeCoordinate(x = it.x, y = it.y)
                },
            updatedAt = Timestamp(updatedAt),
        ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
