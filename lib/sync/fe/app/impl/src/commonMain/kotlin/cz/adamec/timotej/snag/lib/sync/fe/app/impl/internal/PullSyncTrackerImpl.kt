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

package cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.sync.fe.app.api.PullSyncTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PullSyncTrackerImpl : PullSyncTracker {
    private val mutex = Mutex()
    private var activeCount = 0
    private val _isPulling = MutableStateFlow(false)
    override val isPulling: StateFlow<Boolean> = _isPulling

    override suspend fun <T> track(block: suspend () -> T): T {
        mutex.withLock {
            activeCount++
            _isPulling.value = true
        }
        try {
            return block()
        } finally {
            mutex.withLock {
                activeCount--
                _isPulling.value = activeCount > 0
            }
        }
    }
}
