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

package cz.adamec.timotej.snag.structures.be.driven.impl.internal

import org.jetbrains.exposed.sql.Table

internal object StructuresTable : Table("structures") {
    val id = varchar("id", 36)
    val projectId = varchar("project_id", 36)
    val name = varchar("name", 255)
    val floorPlanUrl = varchar("floor_plan_url", 1024).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
