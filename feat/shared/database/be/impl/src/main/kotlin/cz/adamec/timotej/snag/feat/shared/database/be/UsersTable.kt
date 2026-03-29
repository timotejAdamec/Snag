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

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

private const val VARCHAR_MAX_LENGTH = 255

object UsersTable : UuidTable("users") {
    val authProviderId = varchar("entra_id", VARCHAR_MAX_LENGTH).uniqueIndex()
    val email = varchar("email", VARCHAR_MAX_LENGTH)
    val role = varchar("role", VARCHAR_MAX_LENGTH).nullable()
    val updatedAt = long("updated_at")
}
