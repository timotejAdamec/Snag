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

package cz.adamec.timotej.snag.findings.fe.app.api

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GetFindingsUseCase {
    operator fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<List<Finding>>>
}
