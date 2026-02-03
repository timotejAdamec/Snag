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
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ProjectsDb {
    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> =
        projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<ProjectEntity>, OfflineFirstDataResult<List<Project>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toBusiness() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading projects from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveProjects(projects: List<Project>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher, LH.logger, "Error saving projects $projects to DB.") {
            projectEntityQueries.transaction {
                projects.forEach {
                    projectEntityQueries.save(it.toEntity())
                }
            }
        }

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<Project?>> =
        projectEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<ProjectEntity?, OfflineFirstDataResult<Project?>> {
                OfflineFirstDataResult.Success(it?.toBusiness())
            }.catch { e ->
                LH.logger.e { "Error loading project $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveProject(project: Project): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher, LH.logger, "Error saving project $project to DB.") {
            projectEntityQueries.save(project.toEntity())
        }

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher, LH.logger, "Error deleting project $id from DB.") {
            projectEntityQueries.deleteById(id.toString())
        }
}
