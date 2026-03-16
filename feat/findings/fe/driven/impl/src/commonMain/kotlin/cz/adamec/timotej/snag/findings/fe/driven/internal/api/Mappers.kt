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

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.Importance
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.model.Term
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH

internal fun FindingApiDto.toModel(): AppFinding {
    val apiKey =
        try {
            FindingTypeDtoKey.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.e { "Unknown finding type from API: '$type', defaulting to Classic" }
            null
        }
    val findingType =
        when (apiKey) {
            FindingTypeDtoKey.CLASSIC ->
                FindingType.Classic(
                    importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                    term = term?.let { Term.valueOf(it) } ?: Term.T1,
                )

            FindingTypeDtoKey.UNVISITED -> FindingType.Unvisited
            FindingTypeDtoKey.NOTE -> FindingType.Note
            null -> FindingType.Classic()
        }
    return AppFindingData(
        id = id,
        structureId = structureId,
        name = name,
        description = description,
        type = findingType,
        coordinates = coordinates.map { it.toBusiness() }.toSet(),
        updatedAt = updatedAt,
    )
}

internal fun RelativeCoordinateApiDto.toBusiness() =
    RelativeCoordinate(
        x = x,
        y = y,
    )

internal fun FindingType.toDtoKey(): FindingTypeDtoKey =
    when (this) {
        is FindingType.Classic -> FindingTypeDtoKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeDtoKey.UNVISITED
        is FindingType.Note -> FindingTypeDtoKey.NOTE
    }

internal fun AppFinding.toPutApiDto(): PutFindingApiDto {
    val classic = type as? FindingType.Classic
    return PutFindingApiDto(
        structureId = structureId,
        type = type.toDtoKey().name,
        name = name,
        description = description,
        importance = classic?.importance?.name,
        term = classic?.term?.name,
        coordinates = coordinates.map { RelativeCoordinateApiDto(x = it.x, y = it.y) }.toSet(),
        updatedAt = updatedAt,
    )
}
