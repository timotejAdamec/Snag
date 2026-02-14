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

private const val PARTICIPANTS_MAX_LENGTH = 1024
private const val CLIMATE_MAX_LENGTH = 512
private const val NOTE_MAX_LENGTH = 4096

object InspectionsTable : UuidTable("inspections") {
    val project = reference("project_id", ProjectsTable)
    val startedAt = long("started_at").nullable()
    val endedAt = long("ended_at").nullable()
    val participants = varchar("participants", PARTICIPANTS_MAX_LENGTH).nullable()
    val climate = varchar("climate", CLIMATE_MAX_LENGTH).nullable()
    val note = varchar("note", NOTE_MAX_LENGTH).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}
