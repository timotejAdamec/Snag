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
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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
            FindingEntity.find {
                FindingsTable.structureId eq structureId.toJavaUuid()
            }.with(FindingEntity::coordinates).map { it.toBackendFinding() }
        }

    @Suppress("ReturnCount")
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
                    return@transaction existing.toBackendFinding()
                }
                existing.structureId = finding.finding.structureId.toJavaUuid()
                existing.name = finding.finding.name
                existing.description = finding.finding.description
                existing.updatedAt = finding.finding.updatedAt.value
                existing.deletedAt = finding.deletedAt?.value
                existing.coordinates.forEach { it.delete() }
            } else {
                FindingEntity.new(finding.finding.id) {
                    structureId = finding.finding.structureId.toJavaUuid()
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

    @Suppress("ReturnCount")
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
                return@transaction existing.toBackendFinding()
            }

            existing.deletedAt = deletedAt.value
            null
        }

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): List<BackendFinding> =
        transaction(database) {
            FindingEntity.find {
                (FindingsTable.structureId eq structureId.toJavaUuid()) and
                    (
                        (FindingsTable.updatedAt greater since.value) or
                            (FindingsTable.deletedAt greater since.value)
                    )
            }.with(FindingEntity::coordinates).map { it.toBackendFinding() }
        }

    private fun FindingEntity.toBackendFinding(): BackendFinding =
        BackendFinding(
            finding =
                Finding(
                    id = id.value,
                    structureId = structureId.toKotlinUuid(),
                    name = name,
                    description = description,
                    coordinates =
                        coordinates.map {
                            RelativeCoordinate(x = it.x, y = it.y)
                        },
                    updatedAt = Timestamp(updatedAt),
                ),
            deletedAt = deletedAt?.let { Timestamp(it) },
        )
}
