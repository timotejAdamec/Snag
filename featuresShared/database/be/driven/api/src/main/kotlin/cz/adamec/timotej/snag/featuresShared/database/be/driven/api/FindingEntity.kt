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

package cz.adamec.timotej.snag.featuresShared.database.be.driven.api

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class FindingEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var structure by StructureEntity referencedOn FindingsTable.structure
    var type by FindingsTable.type
    var name by FindingsTable.name
    var description by FindingsTable.description
    var updatedAt by FindingsTable.updatedAt
    var deletedAt by FindingsTable.deletedAt
    val coordinates by FindingCoordinateEntity referrersOn
        FindingCoordinatesTable.finding

    companion object : UuidEntityClass<FindingEntity>(FindingsTable)
}

class ClassicFindingEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var importance by ClassicFindingTable.importance
    var term by ClassicFindingTable.term

    companion object : UuidEntityClass<ClassicFindingEntity>(ClassicFindingTable)
}

class FindingPhotoEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var finding by FindingEntity referencedOn FindingPhotosTable.finding
    var url by FindingPhotosTable.url
    var createdAt by FindingPhotosTable.createdAt
    var deletedAt by FindingPhotosTable.deletedAt

    companion object : UuidEntityClass<FindingPhotoEntity>(FindingPhotosTable)
}

class FindingCoordinateEntity(
    id: EntityID<Int>,
) : IntEntity(id) {
    var finding by FindingEntity referencedOn FindingCoordinatesTable.finding
    var x by FindingCoordinatesTable.x
    var y by FindingCoordinatesTable.y

    companion object : IntEntityClass<FindingCoordinateEntity>(FindingCoordinatesTable)
}
