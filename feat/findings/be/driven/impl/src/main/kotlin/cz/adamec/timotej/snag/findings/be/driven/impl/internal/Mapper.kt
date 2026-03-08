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
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.shared.database.be.ClassicFindingEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal enum class FindingTypeDbValue {
    CLASSIC,
    UNVISITED,
    NOTE,
}

internal fun FindingType.toDbValue(): FindingTypeDbValue =
    when (this) {
        is FindingType.Classic -> FindingTypeDbValue.CLASSIC
        is FindingType.Unvisited -> FindingTypeDbValue.UNVISITED
        is FindingType.Note -> FindingTypeDbValue.NOTE
    }

internal fun FindingEntity.toModel(): BackendFinding {
    val dbValue =
        try {
            FindingTypeDbValue.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.error("Unknown finding type in DB: '{}', defaulting to Classic", type)
            null
        }
    val findingType =
        when (dbValue) {
            FindingTypeDbValue.CLASSIC -> {
                val classic = ClassicFindingEntity.findById(id)
                if (classic != null) {
                    FindingType.Classic(
                        importance = Importance.valueOf(classic.importance),
                        term = Term.valueOf(classic.term),
                    )
                } else {
                    FindingType.Classic()
                }
            }
            FindingTypeDbValue.UNVISITED -> {
                FindingType.Unvisited
            }
            FindingTypeDbValue.NOTE -> {
                FindingType.Note
            }
            null -> {
                FindingType.Classic()
            }
        }
    return BackendFinding(
        finding =
            Finding(
                id = id.value,
                structureId = structure.id.value,
                name = name,
                description = description,
                type = findingType,
                coordinates =
                    coordinates.map {
                        RelativeCoordinate(x = it.x, y = it.y)
                    },
                updatedAt = Timestamp(updatedAt),
            ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
}
