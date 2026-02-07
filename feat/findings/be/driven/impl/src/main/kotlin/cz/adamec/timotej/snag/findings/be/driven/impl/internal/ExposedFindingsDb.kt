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
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

internal class ExposedFindingsDb(
    private val database: Database,
) : FindingsDb {
    init {
        transaction(database) {
            SchemaUtils.create(FindingsTable, FindingCoordinatesTable)
        }
    }

    override suspend fun getFindings(structureId: Uuid): List<BackendFinding> =
        transaction(database) {
            val findings =
                FindingsTable
                    .selectAll()
                    .where { FindingsTable.structureId eq structureId.toString() }
                    .map { it.toBackendFindingWithoutCoordinates() }

            findings.map { it.withCoordinates() }
        }

    @Suppress("ReturnCount")
    override suspend fun updateFinding(finding: BackendFinding): BackendFinding? =
        transaction(database) {
            val existing = findById(finding.finding.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        existing.finding.updatedAt,
                        existing.deletedAt ?: Timestamp(0),
                    )
                if (serverTimestamp >= finding.finding.updatedAt) {
                    return@transaction existing
                }
            }

            FindingsTable.upsert {
                it[id] = finding.finding.id.toString()
                it[structureId] = finding.finding.structureId.toString()
                it[name] = finding.finding.name
                it[description] = finding.finding.description
                it[updatedAt] = finding.finding.updatedAt.value
                it[deletedAt] = finding.deletedAt?.value
            }

            FindingCoordinatesTable.deleteWhere {
                findingId eq finding.finding.id.toString()
            }
            finding.finding.coordinates.forEachIndexed { index, coordinate ->
                FindingCoordinatesTable.insert {
                    it[findingId] = finding.finding.id.toString()
                    it[x] = coordinate.x
                    it[y] = coordinate.y
                    it[orderIndex] = index
                }
            }
            null
        }

    @Suppress("ReturnCount")
    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFinding? =
        transaction(database) {
            val existing = findById(id) ?: return@transaction null
            if (existing.deletedAt != null) return@transaction null
            if (existing.finding.updatedAt >= deletedAt) return@transaction existing

            FindingsTable.update({ FindingsTable.id eq id.toString() }) {
                it[FindingsTable.deletedAt] = deletedAt.value
            }
            null
        }

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): List<BackendFinding> =
        transaction(database) {
            val findings =
                FindingsTable
                    .selectAll()
                    .where {
                        (FindingsTable.structureId eq structureId.toString()) and
                            (
                                (FindingsTable.updatedAt greater since.value) or
                                    (FindingsTable.deletedAt greater since.value)
                            )
                    }
                    .map { it.toBackendFindingWithoutCoordinates() }

            findings.map { it.withCoordinates() }
        }

    private fun findById(id: Uuid): BackendFinding? {
        val finding =
            FindingsTable
                .selectAll()
                .where { FindingsTable.id eq id.toString() }
                .map { it.toBackendFindingWithoutCoordinates() }
                .singleOrNull()
                ?: return null
        return finding.withCoordinates()
    }

    private fun BackendFinding.withCoordinates(): BackendFinding {
        val coordinates =
            FindingCoordinatesTable
                .selectAll()
                .where { FindingCoordinatesTable.findingId eq finding.id.toString() }
                .orderBy(FindingCoordinatesTable.orderIndex)
                .map { row ->
                    RelativeCoordinate(
                        x = row[FindingCoordinatesTable.x],
                        y = row[FindingCoordinatesTable.y],
                    )
                }
        return copy(finding = finding.copy(coordinates = coordinates))
    }

    private fun ResultRow.toBackendFindingWithoutCoordinates(): BackendFinding =
        BackendFinding(
            finding =
                Finding(
                    id = Uuid.parse(this[FindingsTable.id]),
                    structureId = Uuid.parse(this[FindingsTable.structureId]),
                    name = this[FindingsTable.name],
                    description = this[FindingsTable.description],
                    coordinates = emptyList(),
                    updatedAt = Timestamp(this[FindingsTable.updatedAt]),
                ),
            deletedAt = this[FindingsTable.deletedAt]?.let { Timestamp(it) },
        )
}
