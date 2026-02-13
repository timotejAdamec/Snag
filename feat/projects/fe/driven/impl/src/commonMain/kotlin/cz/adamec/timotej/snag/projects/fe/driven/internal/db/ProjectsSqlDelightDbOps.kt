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

import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightDbOps
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import kotlinx.coroutines.CoroutineDispatcher

internal class ProjectsSqlDelightDbOps(
    queries: ProjectEntityQueries,
    ioDispatcher: CoroutineDispatcher,
) : SqlDelightDbOps<cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity, FrontendProject>(
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
)
