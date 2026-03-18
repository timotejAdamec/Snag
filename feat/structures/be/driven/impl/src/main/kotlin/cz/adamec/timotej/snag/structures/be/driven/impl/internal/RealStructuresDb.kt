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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.be.StructureEntity
import cz.adamec.timotej.snag.feat.shared.database.be.StructuresTable
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForSaveRequest
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealStructuresDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
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

    override suspend fun saveStructure(backendStructure: BackendStructure): BackendStructure? =
        transaction(database) {
            val existing = StructureEntity.findById(backendStructure.id)
            when (
                val result =
                    resolveConflictForSave(
                        ResolveConflictForSaveRequest(
                            existing = existing?.toModel(),
                            incoming = backendStructure,
                        ),
                    )
            ) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.project = ProjectEntity[backendStructure.projectId]
                        existing.name = backendStructure.name
                        existing.floorPlanUrl = backendStructure.floorPlanUrl
                        existing.updatedAt = backendStructure.updatedAt.value
                        existing.deletedAt = backendStructure.deletedAt?.value
                    } else {
                        StructureEntity.new(backendStructure.id) {
                            project = ProjectEntity[backendStructure.projectId]
                            name = backendStructure.name
                            floorPlanUrl = backendStructure.floorPlanUrl
                            updatedAt = backendStructure.updatedAt.value
                            deletedAt = backendStructure.deletedAt?.value
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendStructure? =
        transaction(database) {
            val existing = StructureEntity.findById(id)
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
