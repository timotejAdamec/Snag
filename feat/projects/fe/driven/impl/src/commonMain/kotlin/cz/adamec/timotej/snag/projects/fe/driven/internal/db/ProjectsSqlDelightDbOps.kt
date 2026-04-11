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

import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.ProjectEntity
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.ProjectEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightDbOps
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class ProjectsSqlDelightDbOps(
    private val queries: ProjectEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : SqlDelightDbOps<ProjectEntity, AppProject>(
        ioDispatcher = ioDispatcher,
        logger = LH.logger,
        entityName = "project",
        mapToModel = { it.toModel() },
        mapToEntity = { it.toEntity() },
        selectAllQuery = { queries.selectAll() },
        selectByIdQuery = { queries.selectById(it) },
        saveEntity = { queries.save(it) },
        deleteEntityById = { queries.deleteById(it) },
        runInTransaction = { block -> queries.transaction { block() } },
    ) {
    suspend fun existsByClientId(clientId: Uuid): Boolean =
        withContext(ioDispatcher) {
            queries
                .existsByClientId(clientId.toString())
                .executeAsOne()
        }
}
