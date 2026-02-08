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

package cz.adamec.timotej.snag.structures.be.driven.impl.internal

import kotlin.uuid.Uuid
import org.jetbrains.exposed.dao.UuidEntity
import org.jetbrains.exposed.dao.UuidEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class StructureEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<StructureEntity>(StructuresTable)

    var projectId by StructuresTable.projectId
    var name by StructuresTable.name
    var floorPlanUrl by StructuresTable.floorPlanUrl
    var updatedAt by StructuresTable.updatedAt
    var deletedAt by StructuresTable.deletedAt
}
