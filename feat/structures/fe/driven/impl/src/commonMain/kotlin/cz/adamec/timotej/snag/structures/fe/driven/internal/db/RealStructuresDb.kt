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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntityQueries
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealStructuresDb(
    private val structureEntityQueries: StructureEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : StructuresDb {
    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>> =
        structureEntityQueries
            .selectByProjectId(projectId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<StructureEntity>, OfflineFirstDataResult<List<FrontendStructure>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading structures for project $projectId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveStructures(structures: List<FrontendStructure>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving structures $structures to DB.") {
            structureEntityQueries.transaction {
                structures.forEach {
                    structureEntityQueries.save(it.toEntity())
                }
            }
        }

    override suspend fun saveStructure(structure: FrontendStructure): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving structure $structure to DB.") {
            structureEntityQueries.save(structure.toEntity())
        }

    override suspend fun deleteStructure(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting structure $id from DB.") {
            structureEntityQueries.deleteById(id.toString())
        }

    override fun getStructureFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>> =
        structureEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<StructureEntity?, OfflineFirstDataResult<FrontendStructure?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading structure $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        withContext(ioDispatcher) {
            structureEntityQueries.selectIdsByProjectId(projectId.toString())
                .executeAsList()
                .map { Uuid.parse(it) }
        }

    override suspend fun deleteStructuresByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting structures for project $projectId from DB.") {
            structureEntityQueries.deleteByProjectId(projectId.toString())
        }
}
