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

package cz.adamec.timotej.snag.sync.fe.app.api.handler

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.GetLastPullSyncedAtTimestampRequest
import cz.adamec.timotej.snag.sync.fe.app.api.model.SetLastPullSyncedAtTimestampRequest
import kotlin.uuid.Uuid

abstract class DbApiPullSyncHandler<TChange>(
    private val logger: Logger,
    private val timestampProvider: TimestampProvider,
    private val getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    private val setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
) : PullSyncOperationHandler {
    /**
     * Freeform string used for logging.
     */
    protected abstract val entityName: String

    protected abstract suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<TChange>>

    protected abstract suspend fun applyChange(change: TChange)

    override suspend fun execute(scopeId: Uuid?): PullSyncOperationResult {
        val scopeIdString = scopeId?.toString() ?: ""
        logger.d { "Starting pull sync for ${entityName}s (scopeId=$scopeId)." }
        val since =
            getLastPullSyncedAtTimestampUseCase(
                GetLastPullSyncedAtTimestampRequest(
                    entityType = entityTypeId,
                    scopeId = scopeIdString,
                ),
            ) ?: Timestamp(0)
        val now = timestampProvider.getNowTimestamp()
        logger.d { "Pulling $entityName changes since=$since, now=$now." }

        return when (val result = fetchChangesFromApi(scopeId, since)) {
            is OnlineDataResult.Failure -> {
                logger.w { "Error pulling $entityName changes (scopeId=$scopeId)." }
                PullSyncOperationResult.Failure
            }
            is OnlineDataResult.Success -> {
                val changes = result.data
                logger.d { "Received ${changes.size} $entityName change(s) (scopeId=$scopeId)." }
                changes.forEach { change ->
                    applyChange(change)
                }
                setLastPullSyncedAtTimestampUseCase(
                    SetLastPullSyncedAtTimestampRequest(
                        entityType = entityTypeId,
                        timestamp = now,
                        scopeId = scopeIdString,
                    ),
                )
                logger.d { "Pull sync for ${entityName}s completed (scopeId=$scopeId), updated lastSyncedAt=$now." }
                PullSyncOperationResult.Success
            }
        }
    }
}
