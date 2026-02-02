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

import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Serializable
internal data class CoordinateJson(
    val x: Float,
    val y: Float,
)

internal fun FindingEntity.toBusiness() =
    Finding(
        id = Uuid.parse(id),
        structureId = Uuid.parse(structureId),
        name = name,
        description = description,
        coordinates = parseCoordinates(coordinates),
    )

internal fun Finding.toEntity() =
    FindingEntity(
        id = id.toString(),
        structureId = structureId.toString(),
        name = name,
        description = description,
        coordinates = serializeCoordinates(coordinates),
    )

private fun parseCoordinates(json: String): List<Coordinate> =
    Json.decodeFromString<List<CoordinateJson>>(json).map {
        Coordinate(x = it.x, y = it.y)
    }

internal fun serializeCoordinates(coordinates: List<Coordinate>): String =
    Json.encodeToString(
        coordinates.map { CoordinateJson(x = it.x, y = it.y) },
    )
