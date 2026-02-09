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

@file:Suppress("MatchingDeclarationName")

package cz.adamec.timotej.snag.findings.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectById
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectByStructureId
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

internal const val DB_TYPE_CLASSIC = "CLASSIC"
internal const val DB_TYPE_UNVISITED = "UNVISITED"
internal const val DB_TYPE_NOTE = "NOTE"

@Serializable
internal data class RelativeCoordinateJson(
    val x: Float,
    val y: Float,
)

internal fun SelectByStructureId.toModel() =
    FrontendFinding(
        finding =
            Finding(
                id = Uuid.parse(id),
                structureId = Uuid.parse(structureId),
                name = name,
                description = description,
                type = toFindingType(type, importance, term),
                coordinates = parseCoordinates(coordinates),
                updatedAt = Timestamp(updatedAt),
            ),
    )

internal fun SelectById.toModel() =
    FrontendFinding(
        finding =
            Finding(
                id = Uuid.parse(id),
                structureId = Uuid.parse(structureId),
                name = name,
                description = description,
                type = toFindingType(type, importance, term),
                coordinates = parseCoordinates(coordinates),
                updatedAt = Timestamp(updatedAt),
            ),
    )

private fun toFindingType(
    type: String,
    importance: String?,
    term: String?,
): FindingType =
    when (type) {
        DB_TYPE_CLASSIC ->
            FindingType.Classic(
                importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                term = term?.let { Term.valueOf(it) } ?: Term.T1,
            )
        DB_TYPE_UNVISITED -> FindingType.Unvisited
        DB_TYPE_NOTE -> FindingType.Note
        else -> {
            LH.logger.e { "Unknown finding type in DB: '$type', defaulting to Classic" }
            FindingType.Classic()
        }
    }

internal fun FrontendFinding.toEntity() =
    FindingEntity(
        id = finding.id.toString(),
        structureId = finding.structureId.toString(),
        type = finding.type.toDbString(),
        name = finding.name,
        description = finding.description,
        coordinates = serializeCoordinates(finding.coordinates),
        updatedAt = finding.updatedAt.value,
    )

internal fun FindingType.toDbString(): String =
    when (this) {
        is FindingType.Classic -> DB_TYPE_CLASSIC
        is FindingType.Unvisited -> DB_TYPE_UNVISITED
        is FindingType.Note -> DB_TYPE_NOTE
    }

private fun parseCoordinates(json: String): List<RelativeCoordinate> =
    Json.decodeFromString<List<RelativeCoordinateJson>>(json).map {
        RelativeCoordinate(x = it.x, y = it.y)
    }

internal fun serializeCoordinates(coordinates: List<RelativeCoordinate>): String =
    Json.encodeToString(
        coordinates.map { RelativeCoordinateJson(x = it.x, y = it.y) },
    )
