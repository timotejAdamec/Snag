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
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.Importance
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.model.Term
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import kotlin.uuid.Uuid

internal fun FindingType.toDtoKey(): FindingTypeDtoKey =
    when (this) {
        is FindingType.Classic -> FindingTypeDtoKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeDtoKey.UNVISITED
        is FindingType.Note -> FindingTypeDtoKey.NOTE
    }

internal fun BackendFinding.toDto(): FindingApiDto {
    val classic = type as? FindingType.Classic
    return FindingApiDto(
        id = id,
        structureId = structureId,
        type = type.toDtoKey().name,
        name = name,
        description = description,
        importance = classic?.importance?.name,
        term = classic?.term?.name,
        coordinates = coordinates.map { it.toDto() }.toSet(),
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}

internal fun RelativeCoordinate.toDto() =
    RelativeCoordinateApiDto(
        x = x,
        y = y,
    )

internal fun PutFindingApiDto.toModel(id: Uuid): BackendFinding {
    val apiKey =
        try {
            FindingTypeDtoKey.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.error("Unknown finding type from API: '{}', defaulting to Classic", type)
            null
        }
    val findingType =
        when (apiKey) {
            FindingTypeDtoKey.CLASSIC -> {
                FindingType.Classic(
                    importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                    term = term?.let { Term.valueOf(it) } ?: Term.T1,
                )
            }
            FindingTypeDtoKey.UNVISITED -> {
                FindingType.Unvisited
            }
            FindingTypeDtoKey.NOTE -> {
                FindingType.Note
            }
            null -> {
                FindingType.Classic()
            }
        }
    return BackendFindingData(
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
