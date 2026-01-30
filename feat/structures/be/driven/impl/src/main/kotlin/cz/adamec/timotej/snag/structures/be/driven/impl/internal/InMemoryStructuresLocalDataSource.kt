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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import kotlin.uuid.Uuid

internal class InMemoryStructuresLocalDataSource : StructuresLocalDataSource {
    private val structures =
        mutableListOf(
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                name = "Block A - Ground Floor",
                floorPlanUrl = "https://upload.wikimedia.org/wikipedia/commons/9/9a/Sample_Floorplan.jpg",
            ),
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                name = "Block A - First Floor",
                floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
            ),
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000003"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                name = "Block B - Ground Floor",
                floorPlanUrl = null,
            ),
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000004"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                name = "Main Building - Basement",
                floorPlanUrl = null,
            ),
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000005"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                name = "Main Building - Ground Floor",
                floorPlanUrl = "https://www.thehousedesigners.com/images/plans/01/SCA/bulk/9333/1st-floor_m.webp",
            ),
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000006"),
                projectId = Uuid.parse("00000000-0000-0000-0000-000000000003"),
                name = "Reading Hall - Level 1",
                floorPlanUrl = null,
            ),
        )

    @Suppress("MaxLineLength")
    override suspend fun getStructures(projectId: Uuid): List<Structure> = structures.filter { it.projectId == projectId }

    // TODO check updated timestamp and return the database structure if it is newer
    override suspend fun updateStructure(structure: Structure): Structure? {
        structures.removeIf { it.id == structure.id }
        structures.add(structure)
        return null
    }
}
