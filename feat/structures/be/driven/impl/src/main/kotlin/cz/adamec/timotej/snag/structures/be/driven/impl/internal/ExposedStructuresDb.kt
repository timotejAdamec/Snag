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
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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
            StructureEntity.find {
                StructuresTable.projectId eq projectId.toJavaUuid()
            }.map { it.toModel() }
        }

    @Suppress("ReturnCount")
    override suspend fun saveStructure(backendStructure: BackendStructure): BackendStructure? =
        transaction(database) {
            val existing =
                StructureEntity.findById(backendStructure.structure.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= backendStructure.structure.updatedAt) {
                    return@transaction existing.toModel()
                }
                existing.projectId = backendStructure.structure.projectId.toJavaUuid()
                existing.name = backendStructure.structure.name
                existing.floorPlanUrl = backendStructure.structure.floorPlanUrl
                existing.updatedAt = backendStructure.structure.updatedAt.value
                existing.deletedAt = backendStructure.deletedAt?.value
            } else {
                StructureEntity.new(backendStructure.structure.id) {
                    projectId = backendStructure.structure.projectId.toJavaUuid()
                    name = backendStructure.structure.name
                    floorPlanUrl = backendStructure.structure.floorPlanUrl
                    updatedAt = backendStructure.structure.updatedAt.value
                    deletedAt = backendStructure.deletedAt?.value
                }
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
                StructureEntity.findById(id)
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (Timestamp(existing.updatedAt) >= deletedAt) {
                return@transaction existing.toModel()
            }

            existing.deletedAt = deletedAt.value
            null
        }

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendStructure> =
        transaction(database) {
            StructureEntity.find {
                (StructuresTable.projectId eq projectId.toJavaUuid()) and
                    (
                        (StructuresTable.updatedAt greater since.value) or
                            (StructuresTable.deletedAt greater since.value)
                    )
            }.map { it.toModel() }
        }
}
