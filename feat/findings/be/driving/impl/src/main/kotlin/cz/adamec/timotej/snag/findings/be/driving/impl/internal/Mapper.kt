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

import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.be.driving.contract.CoordinateApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import kotlin.uuid.Uuid

internal fun Finding.toDto() =
    FindingApiDto(
        id = id,
        structureId = structureId,
        name = name,
        description = description,
        coordinates = coordinates.map { it.toDto() },
    )

internal fun Coordinate.toDto() =
    CoordinateApiDto(
        x = x,
        y = y,
    )

internal fun PutFindingApiDto.toBusiness(id: Uuid) =
    Finding(
        id = id,
        structureId = structureId,
        name = name,
        description = description,
        coordinates = coordinates.map { it.toBusiness() },
    )

internal fun CoordinateApiDto.toBusiness() =
    Coordinate(
        x = x,
        y = y,
    )
