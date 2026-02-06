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

package cz.adamec.timotej.snag.findings.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectById
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectByStructureId
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal fun FrontendFinding.toEntity() =
    FindingEntity(
        id = finding.id.toString(),
        structureId = finding.structureId.toString(),
        name = finding.name,
        description = finding.description,
        updatedAt = finding.updatedAt.value,
    )

internal fun List<SelectByStructureId>.toFindingModels(): List<FrontendFinding> =
    groupBy { it.id }.map { (_, rows) ->
        val first = rows.first()
        FrontendFinding(
            finding = Finding(
                id = Uuid.parse(first.id),
                structureId = Uuid.parse(first.structureId),
                name = first.name,
                description = first.description,
                coordinates = rows.toCoordinates(),
                updatedAt = Timestamp(first.updatedAt),
            ),
        )
    }

internal fun List<SelectById>.toFindingModel(): FrontendFinding? {
    if (isEmpty()) return null
    val first = first()
    return FrontendFinding(
        finding = Finding(
            id = Uuid.parse(first.id),
            structureId = Uuid.parse(first.structureId),
            name = first.name,
            description = first.description,
            coordinates = toCoordinatesFromSelectById(),
            updatedAt = Timestamp(first.updatedAt),
        ),
    )
}

private fun List<SelectByStructureId>.toCoordinates(): List<RelativeCoordinate> =
    mapNotNull { row ->
        if (row.x != null && row.y != null) {
            RelativeCoordinate(x = row.x.toFloat(), y = row.y.toFloat())
        } else {
            null
        }
    }

private fun List<SelectById>.toCoordinatesFromSelectById(): List<RelativeCoordinate> =
    mapNotNull { row ->
        if (row.x != null && row.y != null) {
            RelativeCoordinate(x = row.x.toFloat(), y = row.y.toFloat())
        } else {
            null
        }
    }
