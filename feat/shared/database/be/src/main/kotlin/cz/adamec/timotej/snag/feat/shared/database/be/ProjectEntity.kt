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

import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass

class ProjectEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    companion object : UuidEntityClass<ProjectEntity>(ProjectsTable)

    var name by ProjectsTable.name
    var address by ProjectsTable.address
    var updatedAt by ProjectsTable.updatedAt
    var deletedAt by ProjectsTable.deletedAt
    val structures by StructureEntity referrersOn StructuresTable.project
}
