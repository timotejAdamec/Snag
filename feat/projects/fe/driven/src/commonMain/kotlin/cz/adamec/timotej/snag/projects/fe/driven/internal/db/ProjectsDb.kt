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
import cz.adamec.timotej.snag.feat.shared.database.fe.CallDatabaseWithResult
import cz.adamec.timotej.snag.feat.shared.database.fe.DatabaseResult
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectBookkeepingQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectFailedSync
import cz.adamec.timotej.snag.feat.shared.database.fe.catchAsDatabaseResult
import cz.adamec.timotej.snag.lib.core.TimestampProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class ProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val projectBookkeepingQueries: ProjectBookkeepingQueries,
    private val timestampProvider: TimestampProvider,
    private val callDatabaseWithResult: CallDatabaseWithResult,
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
            .catch { }
            .mapToOneOrNull(ioDispatcher)
            .catchAsDatabaseResult()

    suspend fun saveProject(project: ProjectEntity): DatabaseResult<Long> =
        withContext(ioDispatcher) {
            callDatabaseWithResult {
                projectEntityQueries.saveProject(project)
            }
        }

    fun getLastFailedProjectSyncFlow(id: Uuid): Flow<DatabaseResult<Long?>> =
        projectBookkeepingQueries
            .selectMostRecentFailedSync(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .catchAsDatabaseResult()

    suspend fun insertFailedProjectSync(id: Uuid): DatabaseResult<Long> =
        withContext(ioDispatcher) {
            callDatabaseWithResult {
                projectBookkeepingQueries.insertFailedSync(
                    ProjectFailedSync(
                        project_id = id.toString(),
                        timestamp = timestampProvider.getTimestamp().value,
                    )
                )
            }
        }
}
