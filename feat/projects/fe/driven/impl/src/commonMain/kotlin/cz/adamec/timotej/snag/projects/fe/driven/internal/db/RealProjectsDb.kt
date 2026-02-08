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
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val projectEntityQueries: ProjectEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
    private val applicationScope: ApplicationScope,
    private val internetConnectionStatusListener: InternetConnectionStatusListener,
) : ProjectsDb {
    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<FrontendProject>>> =
        projectEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<ProjectEntity>, OfflineFirstDataResult<List<FrontendProject>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading projects from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }.also {
                applicationScope.launch {
                    internetConnectionStatusListener.isConnectedFlow().collect()
                }
            }

    override suspend fun saveProjects(projects: List<FrontendProject>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving projects $projects to DB.") {
            projectEntityQueries.transaction {
                projects.forEach {
                    projectEntityQueries.save(it.toEntity())
                }
            }
        }

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendProject?>> =
        projectEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<ProjectEntity?, OfflineFirstDataResult<FrontendProject?>> {
                OfflineFirstDataResult.Success(it?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading project $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveProject(project: FrontendProject): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving project $project to DB.") {
            projectEntityQueries.save(project.toEntity())
        }

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting project $id from DB.") {
            projectEntityQueries.deleteById(id.toString())
        }
}
