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

package cz.adamec.timotej.snag.findings.be.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.structures.be.driven.test.TEST_STRUCTURE_ID
import kotlin.uuid.Uuid

val TEST_FINDING_ID: Uuid = Uuid.parse("00000000-0000-0000-0003-000000000001")

suspend fun FindingsDb.seedTestFinding(
    id: Uuid = TEST_FINDING_ID,
    structureId: Uuid = TEST_STRUCTURE_ID,
    name: String = "Test Finding",
    description: String? = null,
    type: FindingType = FindingType.Classic(),
    coordinates: Set<RelativeCoordinate> = emptySet(),
    updatedAt: Timestamp = Timestamp(1L),
) {
    saveFinding(
        BackendFindingData(
            id = id,
            structureId = structureId,
            name = name,
            description = description,
            type = type,
            coordinates = coordinates,
            updatedAt = updatedAt,
        ),
    )
}
