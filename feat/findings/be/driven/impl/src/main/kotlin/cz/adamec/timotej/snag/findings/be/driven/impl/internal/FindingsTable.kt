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

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

internal object FindingsTable : UuidTable("findings") {
    val structureId = uuid("structure_id")
    val name = varchar("name", 255)
    val description = varchar("description", 1024).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

internal object FindingCoordinatesTable : IntIdTable("finding_coordinates") {
    val findingId = reference("finding_id", FindingsTable)
    val x = float("x")
    val y = float("y")
    val orderIndex = integer("order_index")
}
