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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.LH
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.shared.database.fe.db.InspectionEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.InspectionEntityQueries
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealInspectionsDb(
    private val inspectionEntityQueries: InspectionEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : InspectionsDb {
    override fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendInspection>>> =
        inspectionEntityQueries
            .selectByProjectId(projectId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<InspectionEntity>, OfflineFirstDataResult<List<FrontendInspection>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading inspections for project $projectId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveInspection(inspection: FrontendInspection): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving inspection $inspection to DB.") {
            inspectionEntityQueries.save(inspection.toEntity())
        }

    override suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting inspection $id from DB.") {
            inspectionEntityQueries.deleteById(id.toString())
        }

    override fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>> =
        inspectionEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<InspectionEntity?, OfflineFirstDataResult<FrontendInspection?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading inspection $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid> =
        withContext(ioDispatcher) {
            val query = inspectionEntityQueries.selectIdsByProjectId(projectId.toString())
            val ids = query.executeAsList()
            ids.map { Uuid.parse(it) }
        }

    override suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = LH.logger,
            errorMessage = "Error deleting inspections for project $projectId from DB.",
        ) {
            inspectionEntityQueries.deleteByProjectId(projectId.toString())
        }
}
