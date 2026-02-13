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

package cz.adamec.timotej.snag.lib.database.fe

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

open class SqlDelightDbOps<Entity : Any, Model>(
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val entityName: String,
    private val mapToModel: (Entity) -> Model,
    private val mapToEntity: (Model) -> Entity,
    private val selectByIdQuery: (String) -> Query<Entity>,
    private val saveEntity: suspend (Entity) -> Unit,
    private val deleteEntityById: suspend (String) -> Unit,
    private val runInTransaction: suspend (suspend () -> Unit) -> Unit,
    private val selectAllQuery: (() -> Query<Entity>)? = null,
) {
    fun allEntitiesFlow(): Flow<OfflineFirstDataResult<List<Model>>> {
        val query =
            selectAllQuery ?: error("selectAllQuery not provided for $entityName")
        return entitiesByQueryFlow(query())
    }

    fun entitiesByQueryFlow(query: Query<Entity>): Flow<OfflineFirstDataResult<List<Model>>> =
        query
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<Entity>, OfflineFirstDataResult<List<Model>>> { entities ->
                OfflineFirstDataResult.Success(entities.map(mapToModel))
            }.catch { e ->
                logger.e { "Error loading ${entityName}s from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    fun entityByIdFlow(id: Uuid): Flow<OfflineFirstDataResult<Model?>> =
        selectByIdQuery(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<Entity?, OfflineFirstDataResult<Model?>> {
                OfflineFirstDataResult.Success(it?.let(mapToModel))
            }.catch { e ->
                logger.e { "Error loading $entityName $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    suspend fun saveOne(model: Model): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error saving $entityName $model to DB.",
        ) {
            saveEntity(mapToEntity(model))
        }

    suspend fun saveMany(models: List<Model>): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error saving ${entityName}s $models to DB.",
        ) {
            runInTransaction {
                models.forEach { saveEntity(mapToEntity(it)) }
            }
        }

    suspend fun deleteById(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error deleting $entityName $id from DB.",
        ) {
            deleteEntityById(id.toString())
        }

    suspend fun deleteByQuery(
        errorMessage: String,
        block: suspend () -> Unit,
    ): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = errorMessage,
        ) {
            block()
        }
}
