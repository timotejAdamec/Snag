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
import cz.adamec.timotej.snag.feat.shared.database.be.FindingsTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal fun FindingType.toDbString(): String =
    when (this) {
        is FindingType.Classic -> FindingsTable.TYPE_CLASSIC
        is FindingType.Unvisited -> FindingsTable.TYPE_UNVISITED
        is FindingType.Note -> FindingsTable.TYPE_NOTE
    }

internal fun FindingEntity.toModel(): BackendFinding {
    val findingType =
        when (type) {
            FindingsTable.TYPE_CLASSIC -> {
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
            FindingsTable.TYPE_UNVISITED -> {
                FindingType.Unvisited
            }
            FindingsTable.TYPE_NOTE -> {
                FindingType.Note
            }
            else -> {
                LH.logger.error("Unknown finding type in DB: '{}', defaulting to Classic", type)
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
