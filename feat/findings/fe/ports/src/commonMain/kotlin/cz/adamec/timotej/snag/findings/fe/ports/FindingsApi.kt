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

package cz.adamec.timotej.snag.findings.fe.ports

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

interface FindingsApi {
    suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<Finding>>

    suspend fun saveFinding(finding: Finding): OnlineDataResult<Finding?>

    suspend fun deleteFinding(id: Uuid): OnlineDataResult<Unit>
}
