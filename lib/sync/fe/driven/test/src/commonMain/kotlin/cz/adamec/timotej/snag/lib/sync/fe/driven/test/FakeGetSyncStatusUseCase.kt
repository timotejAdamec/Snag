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

package cz.adamec.timotej.snag.lib.sync.fe.driven.test

import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetSyncStatusUseCase : GetSyncStatusUseCase {
    private val statusFlow = MutableStateFlow<SyncStatus>(SyncStatus.Synced)

    fun emit(status: SyncStatus) {
        statusFlow.value = status
    }

    override fun invoke(): Flow<SyncStatus> = statusFlow
}
