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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.StructureEntityQueries
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
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
    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<Structure>>> =
        structureEntityQueries
            .selectByProjectId(projectId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<StructureEntity>, OfflineFirstDataResult<List<Structure>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toBusiness() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading structures for project $projectId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveStructures(structures: List<Structure>): OfflineFirstDataResult<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                structureEntityQueries.transaction {
                    structures.forEach {
                        structureEntityQueries.save(it.toEntity())
                    }
                }
            }.fold(
                onSuccess = {
                    OfflineFirstDataResult.Success(
                        data = Unit,
                    )
                },
                onFailure = { e ->
                    LH.logger.e { "Error saving structures $structures to DB." }
                    OfflineFirstDataResult.ProgrammerError(
                        throwable = e,
                    )
                },
            )
        }
}
