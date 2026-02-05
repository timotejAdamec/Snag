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

package cz.adamec.timotej.snag.findings.be.ports

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

interface FindingsLocalDataSource {
    suspend fun getFindings(structureId: Uuid): List<BackendFinding>

    suspend fun updateFinding(finding: BackendFinding): BackendFinding?

    suspend fun deleteFinding(id: Uuid, deletedAt: Timestamp): BackendFinding?
}
