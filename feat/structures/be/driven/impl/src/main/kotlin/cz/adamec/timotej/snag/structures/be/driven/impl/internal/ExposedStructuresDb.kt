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

import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.be.StructureEntity
import cz.adamec.timotej.snag.feat.shared.database.be.StructuresTable
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class ExposedStructuresDb(
    private val database: Database,
) : StructuresDb {
    override suspend fun getStructures(projectId: Uuid): List<BackendStructure> =
        transaction(database) {
            StructureEntity
                .find {
                    StructuresTable.project eq projectId
                }.map { it.toModel() }
        }

    @Suppress("ReturnCount", "LabeledExpression")
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
                existing.project = ProjectEntity[backendStructure.structure.projectId]
                existing.name = backendStructure.structure.name
                existing.floorPlanUrl = backendStructure.structure.floorPlanUrl
                existing.updatedAt = backendStructure.structure.updatedAt.value
                existing.deletedAt = backendStructure.deletedAt?.value
            } else {
                StructureEntity.new(backendStructure.structure.id) {
                    project = ProjectEntity[backendStructure.structure.projectId]
                    name = backendStructure.structure.name
                    floorPlanUrl = backendStructure.structure.floorPlanUrl
                    updatedAt = backendStructure.structure.updatedAt.value
                    deletedAt = backendStructure.deletedAt?.value
                }
            }
            null
        }

    @Suppress("ReturnCount", "LabeledExpression")
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
            @Suppress("UnnecessaryParentheses")
            StructureEntity
                .find {
                    (StructuresTable.project eq projectId) and
                        (
                            (StructuresTable.updatedAt greater since.value) or
                                (StructuresTable.deletedAt greater since.value)
                        )
                }.map { it.toModel() }
        }
}
