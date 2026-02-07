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

package cz.adamec.timotej.snag.projects.be.driven.impl.internal

import org.jetbrains.exposed.sql.Table

internal object ProjectsTable : Table("projects") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val address = varchar("address", 255)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
