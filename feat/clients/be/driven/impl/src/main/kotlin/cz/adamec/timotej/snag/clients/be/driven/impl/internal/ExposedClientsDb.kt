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
import cz.adamec.timotej.snag.feat.shared.database.be.ClientEntity
import cz.adamec.timotej.snag.feat.shared.database.be.ClientsTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class ExposedClientsDb(
    private val database: Database,
) : ClientsDb {
    override suspend fun getClients(): List<BackendClient> =
        transaction(database) {
            ClientEntity.all().map { it.toModel() }
        }

    override suspend fun getClient(id: Uuid): BackendClient? =
        transaction(database) {
            ClientEntity.findById(id)?.toModel()
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun saveClient(client: BackendClient): BackendClient? =
        transaction(database) {
            val existing = ClientEntity.findById(client.client.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= client.client.updatedAt) {
                    return@transaction existing.toModel()
                }
                existing.name = client.client.name
                existing.address = client.client.address
                existing.phoneNumber = client.client.phoneNumber
                existing.email = client.client.email
                existing.updatedAt = client.client.updatedAt.value
                existing.deletedAt = client.deletedAt?.value
            } else {
                ClientEntity.new(client.client.id) {
                    name = client.client.name
                    address = client.client.address
                    phoneNumber = client.client.phoneNumber
                    email = client.client.email
                    updatedAt = client.client.updatedAt.value
                    deletedAt = client.deletedAt?.value
                }
            }
            null
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendClient? =
        transaction(database) {
            val existing =
                ClientEntity.findById(id)
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (Timestamp(existing.updatedAt) >= deletedAt) {
                return@transaction existing.toModel()
            }

            existing.deletedAt = deletedAt.value
            null
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
