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

package cz.adamec.timotej.snag.feat.shared.database.be

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
    var name by FindingsTable.name
    var description by FindingsTable.description
    var importance by FindingsTable.importance
    var term by FindingsTable.term
    var updatedAt by FindingsTable.updatedAt
    var deletedAt by FindingsTable.deletedAt
    val coordinates by FindingCoordinateEntity referrersOn
        FindingCoordinatesTable.finding orderBy
        FindingCoordinatesTable.orderIndex

    companion object : UuidEntityClass<FindingEntity>(FindingsTable)
}

class FindingCoordinateEntity(
    id: EntityID<Int>,
) : IntEntity(id) {
    var finding by FindingEntity referencedOn FindingCoordinatesTable.finding
    var x by FindingCoordinatesTable.x
    var y by FindingCoordinatesTable.y
    var orderIndex by FindingCoordinatesTable.orderIndex

    companion object : IntEntityClass<FindingCoordinateEntity>(FindingCoordinatesTable)
}
