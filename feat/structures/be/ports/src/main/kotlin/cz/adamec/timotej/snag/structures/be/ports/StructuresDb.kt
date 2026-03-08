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

package cz.adamec.timotej.snag.structures.be.ports

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

interface StructuresDb {
    suspend fun getStructures(projectId: Uuid): List<BackendStructure>

    suspend fun getStructure(id: Uuid): BackendStructure?

    suspend fun upsertStructure(backendStructure: BackendStructure)

    suspend fun softDeleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    )

    suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendStructure>
}
