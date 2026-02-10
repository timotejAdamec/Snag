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

package cz.adamec.timotej.snag.findings.fe.driven.internal.api

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingTypeStringApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH

internal fun FindingApiDto.toModel(): FrontendFinding {
    val findingType =
        when (type) {
            FindingTypeStringApiDto.CLASSIC ->
                FindingType.Classic(
                    importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                    term = term?.let { Term.valueOf(it) } ?: Term.T1,
                )

            FindingTypeStringApiDto.UNVISITED -> FindingType.Unvisited
            FindingTypeStringApiDto.NOTE -> FindingType.Note
            else ->
                FindingType.Classic().also {
                    LH.logger.e { "Unknown finding type from API: '$type', defaulting to Classic" }
                }
        }
    return FrontendFinding(
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

internal fun FrontendFinding.toPutApiDto(): PutFindingApiDto {
    val (typeStr, importanceStr, termStr) =
        when (val type = finding.type) {
            is FindingType.Classic ->
                Triple(FindingTypeStringApiDto.CLASSIC, type.importance.name, type.term.name)

            is FindingType.Unvisited -> Triple(FindingTypeStringApiDto.UNVISITED, null, null)
            is FindingType.Note -> Triple(FindingTypeStringApiDto.NOTE, null, null)
        }
    return PutFindingApiDto(
        structureId = finding.structureId,
        type = typeStr,
        name = finding.name,
        description = finding.description,
        importance = importanceStr,
        term = termStr,
        coordinates = finding.coordinates.map { RelativeCoordinateApiDto(x = it.x, y = it.y) },
        updatedAt = finding.updatedAt,
    )
}
