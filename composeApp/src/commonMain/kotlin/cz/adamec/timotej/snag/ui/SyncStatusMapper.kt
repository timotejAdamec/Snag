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

package cz.adamec.timotej.snag.ui

import cz.adamec.timotej.snag.lib.design.fe.scaffold.SyncStatusBarState
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus

internal fun SyncStatus.toBarState(): SyncStatusBarState =
    when (this) {
        SyncStatus.Synced -> SyncStatusBarState.SYNCED
        SyncStatus.Syncing -> SyncStatusBarState.SYNCING
        SyncStatus.Offline -> SyncStatusBarState.OFFLINE
        SyncStatus.Error -> SyncStatusBarState.ERROR
    }
