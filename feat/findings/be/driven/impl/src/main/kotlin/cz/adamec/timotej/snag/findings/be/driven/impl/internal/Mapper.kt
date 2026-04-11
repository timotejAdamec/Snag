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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhotoData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.featShared.database.be.driven.api.ClassicFindingEntity
import cz.adamec.timotej.snag.featShared.database.be.driven.api.FindingEntity
import cz.adamec.timotej.snag.featShared.database.be.driven.api.FindingPhotoEntity

internal fun FindingType.toEntityKey(): FindingTypeEntityKey =
    when (this) {
        is FindingType.Classic -> FindingTypeEntityKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeEntityKey.UNVISITED
        is FindingType.Note -> FindingTypeEntityKey.NOTE
    }

internal fun FindingPhotoEntity.toModel(): BackendFindingPhoto =
    BackendFindingPhotoData(
        id = id.value,
        findingId = finding.id.value,
        url = url,
        createdAt = Timestamp(createdAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )

internal fun FindingEntity.toModel(): BackendFinding {
    val dbKey =
        try {
            FindingTypeEntityKey.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.error("Unknown finding type in DB: '{}', defaulting to Classic", type)
            null
        }
    val findingType =
        when (dbKey) {
            FindingTypeEntityKey.CLASSIC -> {
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
            FindingTypeEntityKey.UNVISITED -> {
                FindingType.Unvisited
            }
            FindingTypeEntityKey.NOTE -> {
                FindingType.Note
            }
            null -> {
                FindingType.Classic()
            }
        }
    return BackendFindingData(
        id = id.value,
        structureId = structure.id.value,
        name = name,
        description = description,
        type = findingType,
        coordinates =
            coordinates
                .map {
                    RelativeCoordinate(x = it.x, y = it.y)
                }.toSet(),
        updatedAt = Timestamp(updatedAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
}
