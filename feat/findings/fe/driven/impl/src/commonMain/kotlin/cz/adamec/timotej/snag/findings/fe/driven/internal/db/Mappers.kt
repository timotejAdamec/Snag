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
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Serializable
internal data class RelativeCoordinateJson(
    val x: Float,
    val y: Float,
)

internal fun FindingEntity.toModel() =
    FrontendFinding(
        finding = Finding(
            id = Uuid.parse(id),
            structureId = Uuid.parse(structureId),
            name = name,
            description = description,
            coordinates = parseCoordinates(coordinates),
            updatedAt = Timestamp(updatedAt),
        ),
    )

internal fun FrontendFinding.toEntity() =
    FindingEntity(
        id = finding.id.toString(),
        structureId = finding.structureId.toString(),
        name = finding.name,
        description = finding.description,
        coordinates = serializeCoordinates(finding.coordinates),
        updatedAt = finding.updatedAt.value,
    )

private fun parseCoordinates(json: String): List<RelativeCoordinate> =
    Json.decodeFromString<List<RelativeCoordinateJson>>(json).map {
        RelativeCoordinate(x = it.x, y = it.y)
    }

internal fun serializeCoordinates(coordinates: List<RelativeCoordinate>): String =
    Json.encodeToString(
        coordinates.map { RelativeCoordinateJson(x = it.x, y = it.y) },
    )
