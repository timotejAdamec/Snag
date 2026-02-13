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

import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightEntityDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
internal class RealProjectsDb(
    private val queries: ProjectEntityQueries,
    ioDispatcher: CoroutineDispatcher,
) : SqlDelightEntityDb<ProjectEntity, FrontendProject>(ioDispatcher, LH.logger, "project"),
    ProjectsDb {
    override fun selectAllQuery() = queries.selectAll()

    override fun selectByIdQuery(id: String) = queries.selectById(id)

    override suspend fun saveEntity(entity: ProjectEntity) {
        queries.save(entity)
    }

    override suspend fun deleteEntityById(id: String) {
        queries.deleteById(id)
    }

    override suspend fun runInTransaction(block: suspend () -> Unit) {
        queries.transaction { block() }
    }

    override fun mapToModel(entity: ProjectEntity) = entity.toModel()

    override fun mapToEntity(model: FrontendProject) = model.toEntity()

    override fun getAllProjectsFlow() = allEntitiesFlow()

    override fun getProjectFlow(id: Uuid) = entityByIdFlow(id)

    override suspend fun saveProject(project: FrontendProject) = saveOne(project)

    override suspend fun saveProjects(projects: List<FrontendProject>) = saveMany(projects)

    override suspend fun deleteProject(id: Uuid) = deleteById(id)
}
