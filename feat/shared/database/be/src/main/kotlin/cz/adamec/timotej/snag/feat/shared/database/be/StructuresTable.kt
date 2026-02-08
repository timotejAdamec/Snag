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

private const val NAME_MAX_LENGTH = 255
private const val FLOOR_PLAN_URL_MAX_LENGTH = 1024

object StructuresTable : UuidTable("structures") {
    val project = reference("project_id", ProjectsTable)
    val name = varchar("name", NAME_MAX_LENGTH)
    val floorPlanUrl = varchar("floor_plan_url", FLOOR_PLAN_URL_MAX_LENGTH).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}
