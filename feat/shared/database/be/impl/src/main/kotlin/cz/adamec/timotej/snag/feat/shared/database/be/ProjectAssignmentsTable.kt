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

import org.jetbrains.exposed.v1.core.Table

object ProjectAssignmentsTable : Table("project_assignments") {
    val userId = uuid("user_id").references(UsersTable.id)
    val projectId = uuid("project_id").references(ProjectsTable.id)

    override val primaryKey = PrimaryKey(userId, projectId)
}
