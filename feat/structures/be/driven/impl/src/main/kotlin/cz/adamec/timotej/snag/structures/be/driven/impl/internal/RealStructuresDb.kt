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

internal class RealStructuresDb(
    private val database: Database,
) : StructuresDb {
    override suspend fun getStructures(projectId: Uuid): List<BackendStructure> =
        transaction(database) {
            StructureEntity
                .find {
                    StructuresTable.project eq projectId
                }.map { it.toModel() }
        }

    override suspend fun getStructure(id: Uuid): BackendStructure? =
        transaction(database) {
            StructureEntity.findById(id)?.toModel()
        }

    override suspend fun upsertStructure(backendStructure: BackendStructure) {
        transaction(database) {
            val existing = StructureEntity.findById(backendStructure.structure.id)
            if (existing != null) {
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
        }
    }

    override suspend fun softDeleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ) {
        transaction(database) {
            val existing = StructureEntity.findById(id) ?: return@transaction
            existing.deletedAt = deletedAt.value
        }
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
