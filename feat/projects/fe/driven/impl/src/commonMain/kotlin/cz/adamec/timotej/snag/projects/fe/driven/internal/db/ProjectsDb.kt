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
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectBookkeepingQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectFailedSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class ProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val projectBookkeepingQueries: ProjectBookkeepingQueries,
    private val timestampProvider: TimestampProvider,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun getAllProjectsFlow(): Flow<List<Project>> {
        return projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities ->
                entities.map { it.toBusiness() }
            }
    }

    suspend fun saveProjects(projects: List<Project>) =
        withContext(ioDispatcher) {
            projectEntityQueries.transaction {
                projects.forEach { projectEntityQueries.save(it.toEntity()) }
            }
        }

    fun getProjectFlow(id: Uuid): Flow<Project?> =
        projectEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map { it?.toBusiness() }

    suspend fun saveProject(project: Project): Unit =
        withContext(ioDispatcher) {
            projectEntityQueries.save(project.toEntity())
        }

    suspend fun deleteProject(id: Uuid) =
        withContext(ioDispatcher) {
            projectEntityQueries.deleteById(id.toString())
        }

    fun getLastFailedProjectSyncFlow(id: Uuid): Flow<Long?> =
        projectBookkeepingQueries
            .selectMostRecentFailedSync(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)

    suspend fun insertFailedProjectSync(id: Uuid): Long =
        insertFailedProjectSync(
            id = id,
            timestamp = timestampProvider.getTimestamp(),
        )

    suspend fun insertFailedProjectSync(
        id: Uuid,
        timestamp: Timestamp,
    ): Long =
        withContext(ioDispatcher) {
            projectBookkeepingQueries.insertFailedSync(
                ProjectFailedSync(
                    project_id = id.toString(),
                    timestamp = timestamp.value,
                ),
            )
        }

    suspend fun deleteProjectSyncs(projectId: Uuid): Long =
        withContext(ioDispatcher) {
            projectBookkeepingQueries.deleteByProjectId(projectId.toString())
        }

    suspend fun deleteAllProjectsSyncs(): Long =
        withContext(ioDispatcher) {
            projectBookkeepingQueries.deleteAll()
        }
}
