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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.shared.database.be.FindingCoordinateEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingsTable
import cz.adamec.timotej.snag.feat.shared.database.be.StructureEntity
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class ExposedFindingsDb(
    private val database: Database,
) : FindingsDb {
    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        transaction(database) {
            FindingEntity
                .find {
                    FindingsTable.structure eq structureId
                }.with(FindingEntity::coordinates)
                .map { it.toModel() }
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun updateFinding(finding: BackendFinding): BackendFinding? =
        transaction(database) {
            val existing = FindingEntity.findById(finding.finding.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= finding.finding.updatedAt) {
                    return@transaction existing.toModel()
                }
                existing.structure = StructureEntity[finding.finding.structureId]
                existing.name = finding.finding.name
                existing.description = finding.finding.description
                existing.updatedAt = finding.finding.updatedAt.value
                existing.deletedAt = finding.deletedAt?.value
                existing.coordinates.forEach { it.delete() }
            } else {
                FindingEntity.new(finding.finding.id) {
                    structure = StructureEntity[finding.finding.structureId]
                    name = finding.finding.name
                    description = finding.finding.description
                    updatedAt = finding.finding.updatedAt.value
                    deletedAt = finding.deletedAt?.value
                }
            }

            val findingEntity = FindingEntity[finding.finding.id]
            finding.finding.coordinates.forEachIndexed { index, coordinate ->
                FindingCoordinateEntity.new {
                    this.finding = findingEntity
                    x = coordinate.x
                    y = coordinate.y
                    orderIndex = index
                }
            }
            null
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFinding? =
        transaction(database) {
            val existing =
                FindingEntity.findById(id)
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (Timestamp(existing.updatedAt) >= deletedAt) {
                return@transaction existing.toModel()
            }

            existing.deletedAt = deletedAt.value
            null
        }

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): List<BackendFinding> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            FindingEntity
                .find {
                    (FindingsTable.structure eq structureId) and
                        (
                            (FindingsTable.updatedAt greater since.value) or
                                (FindingsTable.deletedAt greater since.value)
                        )
                }.with(FindingEntity::coordinates)
                .map { it.toModel() }
        }
}
