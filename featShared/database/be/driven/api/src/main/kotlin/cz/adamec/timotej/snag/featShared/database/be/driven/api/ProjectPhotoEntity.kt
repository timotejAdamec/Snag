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

package cz.adamec.timotej.snag.featShared.database.be.driven.api

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class ProjectPhotoEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var project by ProjectEntity referencedOn ProjectPhotosTable.project
    var url by ProjectPhotosTable.url
    var description by ProjectPhotosTable.description
    var updatedAt by ProjectPhotosTable.updatedAt
    var deletedAt by ProjectPhotosTable.deletedAt

    companion object : UuidEntityClass<ProjectPhotoEntity>(ProjectPhotosTable)
}
