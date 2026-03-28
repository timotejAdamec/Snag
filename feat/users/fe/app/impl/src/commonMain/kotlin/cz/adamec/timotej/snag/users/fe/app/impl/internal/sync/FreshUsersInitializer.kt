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

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.foundation.fe.Initializer
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.launch

internal class FreshUsersInitializer(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val applicationScope: ApplicationScope,
) : Initializer {
    override suspend fun init() {
        applicationScope.launch {
            executePullSyncUseCase(ExecutePullSyncRequest(entityTypeId = USER_SYNC_ENTITY_TYPE))
        }
    }
}
