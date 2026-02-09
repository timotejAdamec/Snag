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

package cz.adamec.timotej.snag.clients.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.clients.fe.driven.internal.LH
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntityQueries
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class RealClientsDb(
    private val clientEntityQueries: ClientEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ClientsDb {
    override fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<FrontendClient>>> =
        clientEntityQueries
            .selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<ClientEntity>, OfflineFirstDataResult<List<FrontendClient>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading clients from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveClients(clients: List<FrontendClient>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving clients $clients to DB.") {
            clientEntityQueries.transaction {
                clients.forEach {
                    clientEntityQueries.save(it.toEntity())
                }
            }
        }

    override fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>> =
        clientEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<ClientEntity?, OfflineFirstDataResult<FrontendClient?>> {
                OfflineFirstDataResult.Success(it?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading client $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveClient(client: FrontendClient): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving client $client to DB.") {
            clientEntityQueries.save(client.toEntity())
        }

    override suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting client $id from DB.") {
            clientEntityQueries.deleteById(id.toString())
        }
}
