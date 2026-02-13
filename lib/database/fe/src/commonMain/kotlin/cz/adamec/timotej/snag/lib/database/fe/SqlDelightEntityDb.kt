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

@Suppress("TooManyFunctions")
abstract class SqlDelightEntityDb<Entity : Any, Model>(
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val entityName: String,
) {
    protected open fun selectAllQuery(): Query<Entity> =
        throw UnsupportedOperationException("selectAllQuery() not implemented for $entityName")

    protected abstract fun selectByIdQuery(id: String): Query<Entity>

    protected abstract suspend fun saveEntity(entity: Entity)

    protected abstract suspend fun deleteEntityById(id: String)

    protected abstract suspend fun runInTransaction(block: suspend () -> Unit)

    protected abstract fun mapToModel(entity: Entity): Model

    protected abstract fun mapToEntity(model: Model): Entity

    protected fun allEntitiesFlow(): Flow<OfflineFirstDataResult<List<Model>>> =
        selectAllQuery()
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<Entity>, OfflineFirstDataResult<List<Model>>> { entities ->
                OfflineFirstDataResult.Success(entities.map { mapToModel(it) })
            }.catch { e ->
                logger.e { "Error loading ${entityName}s from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    protected fun entitiesByQueryFlow(query: Query<Entity>): Flow<OfflineFirstDataResult<List<Model>>> =
        query
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<Entity>, OfflineFirstDataResult<List<Model>>> { entities ->
                OfflineFirstDataResult.Success(entities.map { mapToModel(it) })
            }.catch { e ->
                logger.e { "Error loading ${entityName}s from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    protected fun entityByIdFlow(id: Uuid): Flow<OfflineFirstDataResult<Model?>> =
        selectByIdQuery(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<Entity?, OfflineFirstDataResult<Model?>> {
                OfflineFirstDataResult.Success(it?.let { mapToModel(it) })
            }.catch { e ->
                logger.e { "Error loading $entityName $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    protected suspend fun saveOne(model: Model): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error saving $entityName to DB.",
        ) {
            saveEntity(mapToEntity(model))
        }

    protected suspend fun saveMany(models: List<Model>): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error saving ${entityName}s to DB.",
        ) {
            runInTransaction {
                models.forEach { saveEntity(mapToEntity(it)) }
            }
        }

    protected suspend fun deleteById(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = logger,
            errorMessage = "Error deleting $entityName $id from DB.",
        ) {
            deleteEntityById(id.toString())
        }

    protected suspend fun deleteByQuery(
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
