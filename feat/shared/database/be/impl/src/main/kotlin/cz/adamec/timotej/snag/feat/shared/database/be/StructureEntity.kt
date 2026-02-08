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
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class StructureEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var project by ProjectEntity referencedOn StructuresTable.project
    var name by StructuresTable.name
    var floorPlanUrl by StructuresTable.floorPlanUrl
    var updatedAt by StructuresTable.updatedAt
    var deletedAt by StructuresTable.deletedAt
    val findings by FindingEntity referrersOn FindingsTable.structure

    companion object : UuidEntityClass<StructureEntity>(StructuresTable)
}
