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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.PullSyncTracker
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SyncCoordinator
import cz.adamec.timotej.snag.users.fe.app.api.PullUserChangesUseCase
import cz.adamec.timotej.snag.users.fe.app.impl.internal.sync.USER_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.users.fe.ports.UserSyncResult
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import cz.adamec.timotej.snag.users.fe.ports.UsersDb

internal class PullUserChangesUseCaseImpl(
    private val usersApi: UsersApi,
    private val usersDb: UsersDb,
    private val getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    private val setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    private val syncCoordinator: SyncCoordinator,
    private val timestampProvider: TimestampProvider,
    private val pullSyncTracker: PullSyncTracker,
) : PullUserChangesUseCase {
    @Suppress("LabeledExpression")
    override suspend operator fun invoke() =
        pullSyncTracker.track {
            LH.logger.d { "Starting pull sync for users." }
            syncCoordinator.withFlushedQueue { wasFlushingSuccessful ->
                if (!wasFlushingSuccessful) {
                    LH.logger.w("Flushing sync queue was not successful, skipping pull sync for users.")
                    return@withFlushedQueue
                }
                val since = getLastPullSyncedAtTimestampUseCase(USER_SYNC_ENTITY_TYPE) ?: Timestamp(0)
                val now = timestampProvider.getNowTimestamp()
                LH.logger.d { "Pulling user changes since=$since, now=$now." }

                when (val result = usersApi.getUsersModifiedSince(since)) {
                    is OnlineDataResult.Failure -> {
                        LH.logger.w { "Error pulling user changes." }
                    }
                    is OnlineDataResult.Success -> {
                        val changes = result.data
                        LH.logger.d { "Received ${changes.size} user change(s)." }
                        changes.forEach { syncResult ->
                            when (syncResult) {
                                is UserSyncResult.Updated -> {
                                    LH.logger.d { "Processing updated user ${syncResult.user.user.id}." }
                                    usersDb.saveUser(syncResult.user)
                                }
                            }
                        }
                        setLastPullSyncedAtTimestampUseCase(
                            entityType = USER_SYNC_ENTITY_TYPE,
                            timestamp = now,
                        )
                        LH.logger.d { "Pull sync for users completed, updated lastSyncedAt=$now." }
                    }
                }
            }
        }
}
