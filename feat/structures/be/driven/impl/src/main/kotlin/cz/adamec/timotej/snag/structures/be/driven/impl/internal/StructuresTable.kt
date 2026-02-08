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

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

private const val NAME_MAX_LENGTH = 255
private const val FLOOR_PLAN_URL_MAX_LENGTH = 1024

internal object StructuresTable : UuidTable("structures") {
    val projectId = uuid("project_id")
    val name = varchar("name", NAME_MAX_LENGTH)
    val floorPlanUrl = varchar("floor_plan_url", FLOOR_PLAN_URL_MAX_LENGTH).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}
