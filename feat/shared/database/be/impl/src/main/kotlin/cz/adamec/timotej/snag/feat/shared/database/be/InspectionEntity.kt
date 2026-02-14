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

class InspectionEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var project by ProjectEntity referencedOn InspectionsTable.project
    var startedAt by InspectionsTable.startedAt
    var endedAt by InspectionsTable.endedAt
    var participants by InspectionsTable.participants
    var climate by InspectionsTable.climate
    var note by InspectionsTable.note
    var updatedAt by InspectionsTable.updatedAt
    var deletedAt by InspectionsTable.deletedAt

    companion object : UuidEntityClass<InspectionEntity>(InspectionsTable)
}
