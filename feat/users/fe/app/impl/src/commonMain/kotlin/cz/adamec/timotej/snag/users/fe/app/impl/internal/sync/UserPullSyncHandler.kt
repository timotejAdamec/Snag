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

package cz.adamec.timotej.snag.users.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import cz.adamec.timotej.snag.users.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.users.fe.ports.UserSyncResult
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlin.uuid.Uuid

internal class UserPullSyncHandler(
    private val usersApi: UsersApi,
    private val usersDb: UsersDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<UserSyncResult>(
        logger = LH.logger,
        timestampProvider = timestampProvider,
        getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
        setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
    ) {
    override val entityTypeId: String = USER_SYNC_ENTITY_TYPE
    override val entityName: String = "user"

    override suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<UserSyncResult>> = usersApi.getUsersModifiedSince(since)

    override suspend fun applyChange(change: UserSyncResult) {
        when (change) {
            is UserSyncResult.Updated -> {
                LH.logger.d { "Processing updated user ${change.user.id}." }
                usersDb.saveUser(change.user)
            }
        }
    }
}
