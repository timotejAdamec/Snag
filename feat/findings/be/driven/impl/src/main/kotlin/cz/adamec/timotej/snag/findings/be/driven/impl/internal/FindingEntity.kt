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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import kotlin.uuid.Uuid
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UuidEntity
import org.jetbrains.exposed.dao.UuidEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class FindingEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<FindingEntity>(FindingsTable)

    var structureId by FindingsTable.structureId
    var name by FindingsTable.name
    var description by FindingsTable.description
    var updatedAt by FindingsTable.updatedAt
    var deletedAt by FindingsTable.deletedAt
    val coordinates by FindingCoordinateEntity referrersOn
        FindingCoordinatesTable.findingId orderBy
        FindingCoordinatesTable.orderIndex
}

internal class FindingCoordinateEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FindingCoordinateEntity>(FindingCoordinatesTable)

    var finding by FindingEntity referencedOn FindingCoordinatesTable.findingId
    var x by FindingCoordinatesTable.x
    var y by FindingCoordinatesTable.y
    var orderIndex by FindingCoordinatesTable.orderIndex
}
