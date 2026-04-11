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

package cz.adamec.timotej.snag.featuresShared.database.be.driven.api

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object ProjectPhotosTable : UuidTable("project_photos") {
    val project = reference("project_id", ProjectsTable).index()
    val url = varchar("url", URL_MAX_LENGTH)
    val description = text("description", eagerLoading = true)
    val updatedAt = long("updated_at").index()
    val deletedAt = long("deleted_at").nullable().index()
}
