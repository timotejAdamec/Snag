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

package cz.adamec.timotej.snag.structures.be.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid

val TEST_STRUCTURE_ID: Uuid = Uuid.parse("00000000-0000-0000-0002-000000000001")

suspend fun StructuresDb.seedTestStructure(
    id: Uuid = TEST_STRUCTURE_ID,
    projectId: Uuid = TEST_PROJECT_ID,
    name: String = "Test Structure",
    floorPlanUrl: String? = null,
    updatedAt: Timestamp = Timestamp(1L),
) {
    saveStructure(
        BackendStructureData(
            id = id,
            projectId = projectId,
            name = name,
            floorPlanUrl = floorPlanUrl,
            updatedAt = updatedAt,
        ),
    )
}
