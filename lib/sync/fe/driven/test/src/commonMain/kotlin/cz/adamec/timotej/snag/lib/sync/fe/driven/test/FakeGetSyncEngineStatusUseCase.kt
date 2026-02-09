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

import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncEngineStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncEngineStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeGetSyncEngineStatusUseCase : GetSyncEngineStatusUseCase {
    private val statusFlow = MutableStateFlow<SyncEngineStatus>(SyncEngineStatus.Idle)

    fun emit(status: SyncEngineStatus) {
        statusFlow.value = status
    }

    override fun invoke(): StateFlow<SyncEngineStatus> = statusFlow.asStateFlow()
}
