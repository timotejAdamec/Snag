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
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightEntityDb
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
internal class RealStructuresDb(
    private val queries: StructureEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : SqlDelightEntityDb<StructureEntity, FrontendStructure>(ioDispatcher, LH.logger, "structure"),
    StructuresDb {
    override fun selectByIdQuery(id: String) = queries.selectById(id)

    override suspend fun saveEntity(entity: StructureEntity) {
        queries.save(entity)
    }

    override suspend fun deleteEntityById(id: String) {
        queries.deleteById(id)
    }

    override suspend fun runInTransaction(block: suspend () -> Unit) {
        queries.transaction { block() }
    }

    override fun mapToModel(entity: StructureEntity) = entity.toModel()

    override fun mapToEntity(model: FrontendStructure) = model.toEntity()

    override fun getStructuresFlow(projectId: Uuid) = entitiesByQueryFlow(queries.selectByProjectId(projectId.toString()))

    override fun getStructureFlow(id: Uuid) = entityByIdFlow(id)

    override suspend fun saveStructure(structure: FrontendStructure) = saveOne(structure)

    override suspend fun saveStructures(structures: List<FrontendStructure>) = saveMany(structures)

    override suspend fun deleteStructure(id: Uuid) = deleteById(id)

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        withContext(ioDispatcher) {
            val ids = queries.selectIdsByProjectId(projectId.toString())
            ids
                .executeAsList()
                .map { Uuid.parse(it) }
        }

    override suspend fun deleteStructuresByProjectId(projectId: Uuid) =
        deleteByQuery("Error deleting structures for project $projectId from DB.") {
            queries.deleteByProjectId(projectId.toString())
        }
}
