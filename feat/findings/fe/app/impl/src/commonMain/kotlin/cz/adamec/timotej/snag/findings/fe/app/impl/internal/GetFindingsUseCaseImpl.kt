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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GetFindingsUseCaseImpl(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
    private val applicationScope: ApplicationScope,
) : GetFindingsUseCase {
    override operator fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<List<Finding>>> {
        applicationScope.launch {
            when (val remoteFindingsResult = findingsApi.getFindings(structureId)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w("Error fetching findings for structure $structureId, not updating local DB.")
                }

                is OnlineDataResult.Success -> {
                    LH.logger.d { "Fetched ${remoteFindingsResult.data.size} findings for structure $structureId." }
                    findingsDb.saveFindings(remoteFindingsResult.data)
                }
            }
        }
        return findingsDb
            .getFindingsFlow(structureId)
            .distinctUntilChanged()
    }
}
