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
import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

internal class MainViewModel(
    getSyncStatus: GetSyncStatusUseCase,
) : ViewModel() {
    val syncStatus: StateFlow<SyncStatus> =
        getSyncStatus()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncStatus.Synced)
}
