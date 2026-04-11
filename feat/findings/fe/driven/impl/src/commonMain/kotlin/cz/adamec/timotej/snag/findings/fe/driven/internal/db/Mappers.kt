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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.FindingEntity
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.FindingPhotoEntity
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.SelectById
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.SelectByStructureId
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
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

internal fun FindingPhotoEntity.toModel(): AppFindingPhoto =
    AppFindingPhotoData(
        id = Uuid.parse(id),
        findingId = Uuid.parse(findingId),
        url = url,
        createdAt = Timestamp(createdAt),
    )
