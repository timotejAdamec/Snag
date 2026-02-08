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

class ClientEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var name by ClientsTable.name
    var address by ClientsTable.address
    var phoneNumber by ClientsTable.phoneNumber
    var email by ClientsTable.email
    var updatedAt by ClientsTable.updatedAt
    var deletedAt by ClientsTable.deletedAt

    companion object : UuidEntityClass<ClientEntity>(ClientsTable)
}
