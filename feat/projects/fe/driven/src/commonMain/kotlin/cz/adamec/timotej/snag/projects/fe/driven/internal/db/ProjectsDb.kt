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

package cz.adamec.timotej.snag.projects.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class ProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun getAllProjects(): Flow<List<ProjectEntity>> {
        return projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    suspend fun getProject(id: Uuid): ProjectEntity? = withContext(ioDispatcher) {
        return@withContext projectEntityQueries
            .selectById(id.toString())
            .executeAsOneOrNull()
    }

    suspend fun saveProject(project: ProjectEntity) = withContext(ioDispatcher) {
        projectEntityQueries.saveProject(project)
    }
}
