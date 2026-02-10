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

package cz.adamec.timotej.snag.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.adamec.timotej.snag.lib.design.fe.state.DEFAULT_NO_STATE_SUBSCRIBER_TIMEOUT
import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

internal class MainViewModel(
    getSyncStatus: GetSyncStatusUseCase,
) : ViewModel() {
    val syncStatus: StateFlow<SyncStatus> =
        getSyncStatus()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = DEFAULT_NO_STATE_SUBSCRIBER_TIMEOUT,
                    ),
                initialValue = SyncStatus.Synced,
            )
}
