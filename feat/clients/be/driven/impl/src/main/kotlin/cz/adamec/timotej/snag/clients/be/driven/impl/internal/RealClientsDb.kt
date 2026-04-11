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

package cz.adamec.timotej.snag.clients.be.driven.impl.internal

import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.featuresShared.database.be.driven.api.ClientEntity
import cz.adamec.timotej.snag.featuresShared.database.be.driven.api.ClientsTable
import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForSaveRequest
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealClientsDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : ClientsDb {
    override suspend fun getClients(): List<BackendClient> =
        transaction(database) {
            ClientEntity.all().map { it.toModel() }
        }

    override suspend fun getClient(id: Uuid): BackendClient? =
        transaction(database) {
            ClientEntity.findById(id)?.toModel()
        }

    override suspend fun saveClient(client: BackendClient): BackendClient? =
        transaction(database) {
            val existing = ClientEntity.findById(client.id)
            when (
                val result =
                    resolveConflictForSave(
                        ResolveConflictForSaveRequest(
                            existing = existing?.toModel(),
                            incoming = client,
                        ),
                    )
            ) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.name = client.name
                        existing.address = client.address
                        existing.phoneNumber = client.phoneNumber
                        existing.email = client.email
                        existing.updatedAt = client.updatedAt.value
                        existing.deletedAt = client.deletedAt?.value
                    } else {
                        ClientEntity.new(client.id) {
                            name = client.name
                            address = client.address
                            phoneNumber = client.phoneNumber
                            email = client.email
                            updatedAt = client.updatedAt.value
                            deletedAt = client.deletedAt?.value
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendClient? =
        transaction(database) {
            val existing = ClientEntity.findById(id)
            when (
                val result =
                    resolveConflictForDelete(
                        ResolveConflictForDeleteRequest(
                            existing = existing?.toModel(),
                            deletedAt = deletedAt,
                        ),
                    )
            ) {
                is DeleteConflictResult.Proceed -> {
                    existing!!.deletedAt = deletedAt.value
                    null
                }
                is DeleteConflictResult.NotFound -> {
                    null
                }
                is DeleteConflictResult.AlreadyDeleted -> {
                    null
                }
                is DeleteConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun getClientsModifiedSince(since: Timestamp): List<BackendClient> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            ClientEntity
                .find {
                    (ClientsTable.updatedAt greater since.value) or
                        (ClientsTable.deletedAt greater since.value)
                }.map { it.toModel() }
        }
}
