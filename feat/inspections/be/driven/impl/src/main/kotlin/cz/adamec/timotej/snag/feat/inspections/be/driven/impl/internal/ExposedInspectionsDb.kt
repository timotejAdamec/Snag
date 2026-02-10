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

import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.shared.database.be.InspectionEntity
import cz.adamec.timotej.snag.feat.shared.database.be.InspectionsTable
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class ExposedInspectionsDb(
    private val database: Database,
) : InspectionsDb {
    override suspend fun getInspections(projectId: Uuid): List<BackendInspection> =
        transaction(database) {
            InspectionEntity
                .find {
                    InspectionsTable.project eq projectId
                }.map { it.toModel() }
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun saveInspection(backendInspection: BackendInspection): BackendInspection? =
        transaction(database) {
            val existing =
                InspectionEntity.findById(backendInspection.inspection.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= backendInspection.inspection.updatedAt) {
                    return@transaction existing.toModel()
                }
                existing.project = ProjectEntity[backendInspection.inspection.projectId]
                existing.startedAt = backendInspection.inspection.startedAt?.value
                existing.endedAt = backendInspection.inspection.endedAt?.value
                existing.participants = backendInspection.inspection.participants
                existing.climate = backendInspection.inspection.climate
                existing.note = backendInspection.inspection.note
                existing.updatedAt = backendInspection.inspection.updatedAt.value
                existing.deletedAt = backendInspection.deletedAt?.value
            } else {
                InspectionEntity.new(backendInspection.inspection.id) {
                    project = ProjectEntity[backendInspection.inspection.projectId]
                    startedAt = backendInspection.inspection.startedAt?.value
                    endedAt = backendInspection.inspection.endedAt?.value
                    participants = backendInspection.inspection.participants
                    climate = backendInspection.inspection.climate
                    note = backendInspection.inspection.note
                    updatedAt = backendInspection.inspection.updatedAt.value
                    deletedAt = backendInspection.deletedAt?.value
                }
            }
            null
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendInspection? =
        transaction(database) {
            val existing =
                InspectionEntity.findById(id)
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (Timestamp(existing.updatedAt) >= deletedAt) {
                return@transaction existing.toModel()
            }

            existing.deletedAt = deletedAt.value
            null
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
