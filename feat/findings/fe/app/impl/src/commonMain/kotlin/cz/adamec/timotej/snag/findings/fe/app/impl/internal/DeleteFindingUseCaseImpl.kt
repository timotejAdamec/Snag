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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncDeleteRequest
import kotlin.uuid.Uuid

class DeleteFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : DeleteFindingUseCase {
    override suspend operator fun invoke(findingId: Uuid): OfflineFirstDataResult<Unit> =
        findingsDb
            .deleteFinding(findingId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteFinding, findingsDb.deleteFinding($findingId)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncDeleteUseCase(
                        EnqueueSyncDeleteRequest(
                            entityTypeId = FINDING_SYNC_ENTITY_TYPE,
                            entityId = findingId,
                        ),
                    )
                }
            }
}
