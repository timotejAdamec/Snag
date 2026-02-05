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
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import kotlin.uuid.Uuid

internal class InMemoryStructuresLocalDataSource(
    timestampProvider: TimestampProvider,
) : StructuresLocalDataSource {
    private val structures =
        mutableListOf(
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = Uuid.parse(PROJECT_1),
                    name = "Block A - Ground Floor",
                    floorPlanUrl = "https://upload.wikimedia.org/wikipedia/commons/9/9a/Sample_Floorplan.jpg",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                    projectId = Uuid.parse(PROJECT_1),
                    name = "Block A - First Floor",
                    floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000003"),
                    projectId = Uuid.parse(PROJECT_1),
                    name = "Block B - Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000004"),
                    projectId = Uuid.parse(PROJECT_2),
                    name = "Main Building - Basement",
                    floorPlanUrl = null,
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000005"),
                    projectId = Uuid.parse(PROJECT_2),
                    name = "Main Building - Ground Floor",
                    floorPlanUrl = "https://www.thehousedesigners.com/images/plans/01/SCA/bulk/9333/1st-floor_m.webp",
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
            BackendStructure(
                structure = Structure(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000006"),
                    projectId = Uuid.parse(PROJECT_3),
                    name = "Reading Hall - Level 1",
                    floorPlanUrl = null,
                    updatedAt = timestampProvider.getNowTimestamp(),
                ),
            ),
        )

    override suspend fun getStructures(projectId: Uuid): List<BackendStructure> =
        structures.filter { it.structure.projectId == projectId }

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendStructure? {
        val foundStructure = structures.find { it.structure.id == id }
            ?: return null
        if (foundStructure.deletedAt != null) return null
        if (foundStructure.structure.updatedAt >= deletedAt) return foundStructure

        val index = structures.indexOfFirst { it.structure.id == id }
        structures[index] = foundStructure.copy(deletedAt = deletedAt)
        return null
    }

    override suspend fun saveStructure(backendStructure: BackendStructure): BackendStructure? {
        val foundStructure = structures.find { it.structure.id == backendStructure.structure.id }
        if (foundStructure != null) {
            val serverTimestamp = maxOf(
                foundStructure.structure.updatedAt,
                foundStructure.deletedAt ?: Timestamp(0),
            )
            if (serverTimestamp >= backendStructure.structure.updatedAt) {
                return foundStructure
            }
        }

        structures.removeIf { it.structure.id == backendStructure.structure.id }
        structures.add(backendStructure)
        return null
    }

    override suspend fun getStructuresModifiedSince(projectId: Uuid, since: Timestamp): List<BackendStructure> =
        structures.filter {
            it.structure.projectId == projectId &&
                (it.structure.updatedAt > since || it.deletedAt?.let { d -> d > since } == true)
        }

    private companion object {
        private const val PROJECT_1 = "00000000-0000-0000-0000-000000000001"
        private const val PROJECT_2 = "00000000-0000-0000-0000-000000000002"
        private const val PROJECT_3 = "00000000-0000-0000-0000-000000000003"
    }
}
