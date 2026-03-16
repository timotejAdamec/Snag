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

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.findings.business.model.Importance
import cz.adamec.timotej.snag.feat.findings.business.model.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.model.Term
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectById
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectByStructureId
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal fun SelectByStructureId.toModel(coordinates: Set<RelativeCoordinate>) =
    AppFindingData(
        id = Uuid.parse(id),
        structureId = Uuid.parse(structureId),
        name = name,
        description = description,
        type = toFindingType(type, importance, term),
        coordinates = coordinates,
        updatedAt = Timestamp(updatedAt),
    )

internal fun SelectById.toModel(coordinates: Set<RelativeCoordinate>) =
    AppFindingData(
        id = Uuid.parse(id),
        structureId = Uuid.parse(structureId),
        name = name,
        description = description,
        type = toFindingType(type, importance, term),
        coordinates = coordinates,
        updatedAt = Timestamp(updatedAt),
    )

private fun toFindingType(
    type: String,
    importance: String?,
    term: String?,
): FindingType {
    val dbValue =
        try {
            FindingTypeEntityKey.valueOf(type)
        } catch (_: IllegalArgumentException) {
            LH.logger.e { "Unknown finding type in DB: '$type', defaulting to Classic" }
            return FindingType.Classic()
        }
    return when (dbValue) {
        FindingTypeEntityKey.CLASSIC ->
            FindingType.Classic(
                importance = importance?.let { Importance.valueOf(it) } ?: Importance.MEDIUM,
                term = term?.let { Term.valueOf(it) } ?: Term.T1,
            )

        FindingTypeEntityKey.UNVISITED -> FindingType.Unvisited
        FindingTypeEntityKey.NOTE -> FindingType.Note
    }
}

internal fun AppFinding.toEntity() =
    FindingEntity(
        id = id.toString(),
        structureId = structureId.toString(),
        type = type.toEntityKey().name,
        name = name,
        description = description,
        updatedAt = updatedAt.value,
    )

internal fun FindingType.toEntityKey(): FindingTypeEntityKey =
    when (this) {
        is FindingType.Classic -> FindingTypeEntityKey.CLASSIC
        is FindingType.Unvisited -> FindingTypeEntityKey.UNVISITED
        is FindingType.Note -> FindingTypeEntityKey.NOTE
    }
