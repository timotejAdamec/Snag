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
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto

internal fun FindingApiDto.toModel() =
    FrontendFinding(
        finding = Finding(
            id = id,
            structureId = structureId,
            name = name,
            description = description,
            importance = Importance.valueOf(importance),
            coordinates = coordinates.map { it.toBusiness() },
            updatedAt = updatedAt,
        ),
    )

internal fun RelativeCoordinateApiDto.toBusiness() =
    RelativeCoordinate(
        x = x,
        y = y,
    )

internal fun FrontendFinding.toPutApiDto() =
    PutFindingApiDto(
        structureId = finding.structureId,
        name = finding.name,
        description = finding.description,
        importance = finding.importance.name,
        coordinates = finding.coordinates.map { RelativeCoordinateApiDto(x = it.x, y = it.y) },
        updatedAt = finding.updatedAt,
    )
