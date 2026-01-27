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
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ProjectsDb {
    override fun getAllProjectsFlow(): Flow<List<Project>> {
        return projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .catch { e ->
                LH.logger.e(e) { "Error loading projects from DB." }
                emit(emptyList())
            }
            .map { entities ->
                entities.map { it.toBusiness() }
            }
    }

    override suspend fun saveProjects(projects: List<Project>) =
        withContext(ioDispatcher) {
            projectEntityQueries.transaction {
                projects.forEach {
                    projectEntityQueries.save(it.toEntity())
                }
            }
        }

    override fun getProjectFlow(id: Uuid): Flow<Project?> =
        projectEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .catch { e ->
                LH.logger.e(e) { "Error loading project $id from DB." }
                emit(null)
            }
            .map { it?.toBusiness() }

    override suspend fun saveProject(project: Project): Unit =
        withContext(ioDispatcher) {
            projectEntityQueries.save(project.toEntity())
        }

    override suspend fun deleteProject(id: Uuid): Unit =
        withContext(ioDispatcher) {
            projectEntityQueries.deleteById(id.toString())
        }
}
