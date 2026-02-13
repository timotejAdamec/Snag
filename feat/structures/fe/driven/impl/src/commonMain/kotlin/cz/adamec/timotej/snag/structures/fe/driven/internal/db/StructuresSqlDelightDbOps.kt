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

package cz.adamec.timotej.snag.structures.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntityQueries
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightDbOps
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class StructuresSqlDelightDbOps(
    private val queries: StructureEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : SqlDelightDbOps<StructureEntity, FrontendStructure>(
        ioDispatcher = ioDispatcher,
        logger = LH.logger,
        entityName = "structure",
        mapToModel = { it.toModel() },
        mapToEntity = { it.toEntity() },
        selectByIdQuery = { queries.selectById(it) },
        saveEntity = { queries.save(it) },
        deleteEntityById = { queries.deleteById(it) },
        runInTransaction = { block -> queries.transaction { block() } },
    ) {
    fun structuresByProjectIdFlow(projectId: Uuid) = entitiesByQueryFlow(queries.selectByProjectId(projectId.toString()))

    suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        withContext(ioDispatcher) {
            queries
                .selectIdsByProjectId(projectId.toString())
                .executeAsList()
                .map { Uuid.parse(it) }
        }

    suspend fun deleteStructuresByProjectId(projectId: Uuid) =
        deleteByQuery("Error deleting structures for project $projectId from DB.") {
            queries.deleteByProjectId(projectId.toString())
        }
}
