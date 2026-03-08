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

package cz.adamec.timotej.snag.findings.be.driving.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.FindingTypeKey
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.key
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import kotlin.uuid.Uuid

internal fun BackendFinding.toDto(): FindingApiDto {
    val (typeStr, importanceStr, termStr) =
        when (val type = finding.type) {
            is FindingType.Classic ->
                Triple(type.key.name, type.importance.name, type.term.name)
            is FindingType.Unvisited -> Triple(type.key.name, null, null)
            is FindingType.Note -> Triple(type.key.name, null, null)
        }
    return FindingApiDto(
        id = finding.id,
        structureId = finding.structureId,
        type = typeStr,
        name = finding.name,
        description = finding.description,
        importance = importanceStr,
        term = termStr,
        coordinates = finding.coordinates.map { it.toDto() },
        updatedAt = finding.updatedAt,
        deletedAt = deletedAt,
    )
}

internal fun RelativeCoordinate.toDto() =
    RelativeCoordinateApiDto(
        x = x,
        y = y,
    )

internal fun PutFindingApiDto.toModel(id: Uuid): BackendFinding {
    val typeKey =
        try {
            FindingTypeKey.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.error("Unknown finding type from API: '{}', defaulting to Classic", type)
            null
        }
    val findingType =
        when (typeKey) {
            FindingTypeKey.CLASSIC -> {
                FindingType.Classic(
                    importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                    term = term?.let { Term.valueOf(it) } ?: Term.T1,
                )
            }
            FindingTypeKey.UNVISITED -> {
                FindingType.Unvisited
            }
            FindingTypeKey.NOTE -> {
                FindingType.Note
            }
            null -> {
                FindingType.Classic()
            }
        }
    return BackendFinding(
        finding =
            Finding(
                id = id,
                structureId = structureId,
                name = name,
                description = description,
                type = findingType,
                coordinates = coordinates.map { it.toBusiness() },
                updatedAt = updatedAt,
            ),
    )
}

internal fun RelativeCoordinateApiDto.toBusiness() =
    RelativeCoordinate(
        x = x,
        y = y,
    )
