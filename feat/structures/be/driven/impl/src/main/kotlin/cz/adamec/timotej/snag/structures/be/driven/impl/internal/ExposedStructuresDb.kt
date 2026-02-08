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

package cz.adamec.timotej.snag.structures.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

internal class ExposedStructuresDb(
    private val database: Database,
) : StructuresDb {
    init {
        transaction(database) {
            SchemaUtils.create(StructuresTable)
        }
    }

    override suspend fun getStructures(projectId: Uuid): List<BackendStructure> =
        transaction(database) {
            StructuresTable
                .selectAll()
                .where { StructuresTable.projectId eq projectId.toJavaUuid() }
                .map { it.toBackendStructure() }
        }

    @Suppress("ReturnCount")
    override suspend fun saveStructure(backendStructure: BackendStructure): BackendStructure? =
        transaction(database) {
            val existing =
                StructuresTable
                    .selectAll()
                    .where { StructuresTable.id eq backendStructure.structure.id.toJavaUuid() }
                    .map { it.toBackendStructure() }
                    .singleOrNull()

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        existing.structure.updatedAt,
                        existing.deletedAt ?: Timestamp(0),
                    )
                if (serverTimestamp >= backendStructure.structure.updatedAt) {
                    return@transaction existing
                }
            }

            StructuresTable.upsert {
                it[id] = backendStructure.structure.id.toJavaUuid()
                it[projectId] = backendStructure.structure.projectId.toJavaUuid()
                it[name] = backendStructure.structure.name
                it[floorPlanUrl] = backendStructure.structure.floorPlanUrl
                it[updatedAt] = backendStructure.structure.updatedAt.value
                it[deletedAt] = backendStructure.deletedAt?.value
            }
            null
        }

    @Suppress("ReturnCount")
    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendStructure? =
        transaction(database) {
            val existing =
                StructuresTable
                    .selectAll()
                    .where { StructuresTable.id eq id.toJavaUuid() }
                    .map { it.toBackendStructure() }
                    .singleOrNull()
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (existing.structure.updatedAt >= deletedAt) return@transaction existing

            StructuresTable.update({ StructuresTable.id eq id.toJavaUuid() }) {
                it[StructuresTable.deletedAt] = deletedAt.value
            }
            null
        }

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendStructure> =
        transaction(database) {
            StructuresTable
                .selectAll()
                .where {
                    (StructuresTable.projectId eq projectId.toJavaUuid()) and
                        (
                            (StructuresTable.updatedAt greater since.value) or
                                (StructuresTable.deletedAt greater since.value)
                        )
                }
                .map { it.toBackendStructure() }
        }

    private fun ResultRow.toBackendStructure(): BackendStructure =
        BackendStructure(
            structure =
                Structure(
                    id = this[StructuresTable.id].value.toKotlinUuid(),
                    projectId = this[StructuresTable.projectId].toKotlinUuid(),
                    name = this[StructuresTable.name],
                    floorPlanUrl = this[StructuresTable.floorPlanUrl],
                    updatedAt = Timestamp(this[StructuresTable.updatedAt]),
                ),
            deletedAt = this[StructuresTable.deletedAt]?.let { Timestamp(it) },
        )
}
