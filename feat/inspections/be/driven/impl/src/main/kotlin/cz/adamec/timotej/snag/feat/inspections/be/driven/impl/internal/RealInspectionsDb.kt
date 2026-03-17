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

package cz.adamec.timotej.snag.feat.inspections.be.driven.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.shared.database.be.InspectionEntity
import cz.adamec.timotej.snag.feat.shared.database.be.InspectionsTable
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.sync.be.SaveConflictResult
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealInspectionsDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : InspectionsDb {
    override suspend fun getInspections(projectId: Uuid): List<BackendInspection> =
        transaction(database) {
            InspectionEntity
                .find {
                    InspectionsTable.project eq projectId
                }.map { it.toModel() }
        }

    override suspend fun getInspection(id: Uuid): BackendInspection? =
        transaction(database) {
            InspectionEntity.findById(id)?.toModel()
        }

    override suspend fun saveInspection(backendInspection: BackendInspection): BackendInspection? =
        transaction(database) {
            val existing = InspectionEntity.findById(backendInspection.id)
            when (val result = resolveConflictForSave(existing?.toModel(), backendInspection)) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.project = ProjectEntity[backendInspection.projectId]
                        existing.startedAt = backendInspection.startedAt?.value
                        existing.endedAt = backendInspection.endedAt?.value
                        existing.participants = backendInspection.participants
                        existing.climate = backendInspection.climate
                        existing.note = backendInspection.note
                        existing.updatedAt = backendInspection.updatedAt.value
                        existing.deletedAt = backendInspection.deletedAt?.value
                    } else {
                        InspectionEntity.new(backendInspection.id) {
                            project = ProjectEntity[backendInspection.projectId]
                            startedAt = backendInspection.startedAt?.value
                            endedAt = backendInspection.endedAt?.value
                            participants = backendInspection.participants
                            climate = backendInspection.climate
                            note = backendInspection.note
                            updatedAt = backendInspection.updatedAt.value
                            deletedAt = backendInspection.deletedAt?.value
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendInspection? =
        transaction(database) {
            val existing = InspectionEntity.findById(id)
            when (val result = resolveConflictForDelete(existing?.toModel(), deletedAt)) {
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

    override suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendInspection> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            InspectionEntity
                .find {
                    (InspectionsTable.project eq projectId) and
                        (
                            (InspectionsTable.updatedAt greater since.value) or
                                (InspectionsTable.deletedAt greater since.value)
                        )
                }.map { it.toModel() }
        }
}
