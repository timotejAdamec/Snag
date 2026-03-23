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

package cz.adamec.timotej.snag.feat.inspections.be.driven.test

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import kotlin.uuid.Uuid

val TEST_INSPECTION_ID: Uuid = Uuid.parse("00000000-0000-0000-0004-000000000001")

suspend fun InspectionsDb.seedTestInspection(
    id: Uuid = TEST_INSPECTION_ID,
    projectId: Uuid = TEST_PROJECT_ID,
    startedAt: Timestamp? = null,
    endedAt: Timestamp? = null,
    participants: String? = null,
    climate: String? = null,
    note: String? = null,
    updatedAt: Timestamp = Timestamp(1L),
) {
    saveInspection(
        BackendInspectionData(
            id = id,
            projectId = projectId,
            startedAt = startedAt,
            endedAt = endedAt,
            participants = participants,
            climate = climate,
            note = note,
            updatedAt = updatedAt,
        ),
    )
}
