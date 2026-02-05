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
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.PutFindingApiDto
import cz.adamec.timotej.snag.findings.be.driving.contract.RelativeCoordinateApiDto
import kotlin.uuid.Uuid

internal fun BackendFinding.toDto() =
    FindingApiDto(
        id = finding.id,
        structureId = finding.structureId,
        name = finding.name,
        description = finding.description,
        coordinates = finding.coordinates.map { it.toDto() },
    )

internal fun RelativeCoordinate.toDto() =
    RelativeCoordinateApiDto(
        x = x,
        y = y,
    )

internal fun PutFindingApiDto.toModel(id: Uuid) =
    BackendFinding(
        finding = Finding(
            id = id,
            structureId = structureId,
            name = name,
            description = description,
            coordinates = coordinates.map { it.toBusiness() },
        ),
    )

internal fun RelativeCoordinateApiDto.toBusiness() =
    RelativeCoordinate(
        x = x,
        y = y,
    )
