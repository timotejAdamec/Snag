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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ProjectAssignmentEntityQueries
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealProjectAssignmentsDb(
    private val queries: ProjectAssignmentEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ProjectAssignmentsDb {
    override fun getAssignedUserIdsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<Set<Uuid>>> =
        queries
            .selectByProjectId(projectId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<String>, OfflineFirstDataResult<Set<Uuid>>> { rows ->
                OfflineFirstDataResult.Success(rows.map { Uuid.parse(it) }.toSet())
            }.catch { e ->
                LH.logger.e(e) { "Error querying project assignments for project $projectId." }
                emit(OfflineFirstDataResult.ProgrammerError(e))
            }

    override suspend fun replaceAssignments(
        projectId: Uuid,
        userIds: Set<Uuid>,
    ): OfflineFirstDataResult<Unit> =
        try {
            withContext(ioDispatcher) {
                queries.transaction {
                    queries.deleteByProjectId(projectId.toString())
                    userIds.forEach { userId ->
                        queries.insert(
                            projectId = projectId.toString(),
                            userId = userId.toString(),
                        )
                    }
                }
            }
            OfflineFirstDataResult.Success(Unit)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception,
        ) {
            LH.logger.e(e) { "Error replacing assignments for project $projectId." }
            OfflineFirstDataResult.ProgrammerError(e)
        }

    override suspend fun deleteAssignmentsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        try {
            withContext(ioDispatcher) {
                queries.deleteByProjectId(projectId.toString())
            }
            OfflineFirstDataResult.Success(Unit)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception,
        ) {
            LH.logger.e(e) { "Error deleting assignments for project $projectId." }
            OfflineFirstDataResult.ProgrammerError(e)
        }

    override suspend fun getProjectIdsForAssignedUser(userId: Uuid): Set<Uuid> =
        withContext(ioDispatcher) {
            queries
                .selectProjectIdsByUserId(userId.toString())
                .executeAsList()
                .map { Uuid.parse(it) }
                .toSet()
        }
}
