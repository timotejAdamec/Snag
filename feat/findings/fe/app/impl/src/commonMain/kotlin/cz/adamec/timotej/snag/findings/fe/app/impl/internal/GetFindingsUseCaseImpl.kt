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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.PullFindingChangesUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GetFindingsUseCaseImpl(
    private val pullFindingChangesUseCase: PullFindingChangesUseCase,
    private val findingsDb: FindingsDb,
    private val applicationScope: ApplicationScope,
) : GetFindingsUseCase {
    override operator fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<List<FrontendFinding>>> {
        applicationScope.launch {
            pullFindingChangesUseCase(structureId)
        }
        return findingsDb
            .getFindingsFlow(structureId)
            .distinctUntilChanged()
    }
}
