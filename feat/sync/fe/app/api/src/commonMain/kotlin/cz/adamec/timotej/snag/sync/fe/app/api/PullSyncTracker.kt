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

package cz.adamec.timotej.snag.sync.fe.app.api

import kotlinx.coroutines.flow.StateFlow

interface PullSyncTracker {
    val isPulling: StateFlow<Boolean>

    suspend fun <T> track(block: suspend () -> T): T
}
