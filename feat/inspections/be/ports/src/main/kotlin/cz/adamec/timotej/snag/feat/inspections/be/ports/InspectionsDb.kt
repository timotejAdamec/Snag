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

package cz.adamec.timotej.snag.feat.inspections.be.ports

import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

interface InspectionsDb {
    suspend fun getInspections(projectId: Uuid): List<BackendInspection>

    suspend fun saveInspection(backendInspection: BackendInspection): BackendInspection?

    suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendInspection?

    suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendInspection>
}
