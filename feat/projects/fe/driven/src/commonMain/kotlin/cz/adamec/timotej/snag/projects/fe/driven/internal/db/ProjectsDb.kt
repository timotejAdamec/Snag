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
import cz.adamec.timotej.snag.feat.shared.database.fe.DatabaseResult
import cz.adamec.timotej.snag.feat.shared.database.fe.SuspendCallDatabaseWithResult
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectBookkeepingQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectFailedSync
import cz.adamec.timotej.snag.feat.shared.database.fe.catchAsDatabaseResult
import cz.adamec.timotej.snag.lib.core.Timestamp
import cz.adamec.timotej.snag.lib.core.TimestampProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class ProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val projectBookkeepingQueries: ProjectBookkeepingQueries,
    private val timestampProvider: TimestampProvider,
    private val suspendCallDatabaseWithResult: SuspendCallDatabaseWithResult,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun getAllProjectsFlow(): Flow<DatabaseResult<List<ProjectEntity>>> =
        projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .catchAsDatabaseResult()

    fun getProjectFlow(id: Uuid): Flow<DatabaseResult<ProjectEntity?>> =
        projectEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .catchAsDatabaseResult()

    suspend fun saveProject(project: ProjectEntity): DatabaseResult<Long> =
        suspendCallDatabaseWithResult {
            projectEntityQueries.saveProject(project)
        }

    fun getLastFailedProjectSyncFlow(id: Uuid): Flow<DatabaseResult<Long?>> =
        projectBookkeepingQueries
            .selectMostRecentFailedSync(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .catchAsDatabaseResult()

    suspend fun insertFailedProjectSync(id: Uuid): DatabaseResult<Long> =
        insertFailedProjectSync(
            id = id,
            timestamp = timestampProvider.getTimestamp(),
        )

    suspend fun insertFailedProjectSync(id: Uuid, timestamp: Timestamp): DatabaseResult<Long> =
        suspendCallDatabaseWithResult {
            projectBookkeepingQueries.insertFailedSync(
                ProjectFailedSync(
                    project_id = id.toString(),
                    timestamp = timestamp.value,
                )
            )
        }

    suspend fun deleteProjectSyncs(
        projectId: Uuid,
    ): DatabaseResult<Long> = suspendCallDatabaseWithResult {
        projectBookkeepingQueries.deleteByProjectId(projectId.toString())
    }

    suspend fun deleteAllProjectsSyncs(): DatabaseResult<Long> =
        suspendCallDatabaseWithResult {
            projectBookkeepingQueries.deleteAll()
        }
}
