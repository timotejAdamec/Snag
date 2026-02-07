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

import org.jetbrains.exposed.sql.Table

internal object FindingsTable : Table("findings") {
    val id = varchar("id", 36)
    val structureId = varchar("structure_id", 36)
    val name = varchar("name", 255)
    val description = varchar("description", 1024).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

internal object FindingCoordinatesTable : Table("finding_coordinates") {
    val id = integer("id").autoIncrement()
    val findingId = varchar("finding_id", 36).references(FindingsTable.id)
    val x = float("x")
    val y = float("y")
    val orderIndex = integer("order_index")

    override val primaryKey = PrimaryKey(id)
}
