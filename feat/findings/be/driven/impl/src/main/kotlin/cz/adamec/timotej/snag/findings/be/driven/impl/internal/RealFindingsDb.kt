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
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.shared.database.be.ClassicFindingEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingCoordinateEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingsTable
import cz.adamec.timotej.snag.feat.shared.database.be.StructureEntity
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.lib.sync.be.SaveConflictResult
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealFindingsDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : FindingsDb {
    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        transaction(database) {
            FindingEntity
                .find {
                    FindingsTable.structure eq structureId
                }.with(FindingEntity::coordinates)
                .map { it.toModel() }
        }

    override suspend fun getFinding(id: Uuid): BackendFinding? =
        transaction(database) {
            FindingEntity.findById(id)?.toModel()
        }

    override suspend fun saveFinding(finding: BackendFinding): BackendFinding? =
        transaction(database) {
            val existing = FindingEntity.findById(finding.finding.id)
            when (val result = resolveConflictForSave(existing?.toModel(), finding)) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.structure = StructureEntity[finding.finding.structureId]
                        existing.type =
                            finding.finding.type
                                .toEntityKey()
                                .name
                        existing.name = finding.finding.name
                        existing.description = finding.finding.description
                        existing.updatedAt = finding.finding.updatedAt.value
                        existing.deletedAt = finding.deletedAt?.value
                        existing.coordinates.forEach { it.delete() }
                    } else {
                        FindingEntity.new(finding.finding.id) {
                            structure = StructureEntity[finding.finding.structureId]
                            type =
                                finding.finding.type
                                    .toEntityKey()
                                    .name
                            name = finding.finding.name
                            description = finding.finding.description
                            updatedAt = finding.finding.updatedAt.value
                            deletedAt = finding.deletedAt?.value
                        }
                    }
                    saveClassicFindingDetails(finding)

                    val findingEntity = FindingEntity[finding.finding.id]
                    finding.finding.coordinates.forEach { coordinate ->
                        FindingCoordinateEntity.new {
                            this.finding = findingEntity
                            x = coordinate.x
                            y = coordinate.y
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFinding? =
        transaction(database) {
            val existing = FindingEntity.findById(id)
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

    private fun saveClassicFindingDetails(finding: BackendFinding) {
        val type = finding.finding.type
        if (type is FindingType.Classic) {
            val existing = ClassicFindingEntity.findById(finding.finding.id)
            if (existing != null) {
                existing.importance = type.importance.name
                existing.term = type.term.name
            } else {
                ClassicFindingEntity.new(finding.finding.id) {
                    importance = type.importance.name
                    term = type.term.name
                }
            }
        } else {
            ClassicFindingEntity.findById(finding.finding.id)?.delete()
        }
    }
}
