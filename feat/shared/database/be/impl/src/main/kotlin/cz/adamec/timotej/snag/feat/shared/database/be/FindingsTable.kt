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

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.uuid.Uuid

private const val NAME_MAX_LENGTH = 255
private const val TYPE_MAX_LENGTH = 9
private const val IMPORTANCE_MAX_LENGTH = 6
private const val TERM_MAX_LENGTH = 3

object FindingsTable : UuidTable("findings") {
    const val TYPE_CLASSIC = "CLASSIC"
    const val TYPE_UNVISITED = "UNVISITED"
    const val TYPE_NOTE = "NOTE"

    val structure = reference("structure_id", StructuresTable).index()
    val type = varchar("type", TYPE_MAX_LENGTH).default(TYPE_CLASSIC)
    val name = varchar("name", NAME_MAX_LENGTH)
    val description = text("description", eagerLoading = true).nullable()
    val updatedAt = long("updated_at").index()
    val deletedAt = long("deleted_at").nullable().index()
}

object ClassicFindingTable : IdTable<Uuid>("classic_findings") {
    override val id = reference("finding_id", FindingsTable)
    val importance = varchar("importance", IMPORTANCE_MAX_LENGTH)
    val term = varchar("term", TERM_MAX_LENGTH)
    override val primaryKey = PrimaryKey(id)
}

object FindingCoordinatesTable : IntIdTable("finding_coordinates") {
    val finding = reference("finding_id", FindingsTable)
    val x = float("x")
    val y = float("y")
    val orderIndex = integer("order_index")
}
