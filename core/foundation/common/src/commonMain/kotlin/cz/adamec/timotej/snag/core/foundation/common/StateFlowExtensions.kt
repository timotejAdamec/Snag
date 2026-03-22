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

package cz.adamec.timotej.snag.core.foundation.common

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

/**
 * Synchronous [StateFlow] mapping — [value] always returns the current
 * mapped value of the upstream flow without requiring a coroutine scope.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
    object : StateFlow<R> {
        override val value: R get() = transform(this@mapState.value)

        override val replayCache: List<R> get() = this@mapState.replayCache.map(transform)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@mapState.collect { collector.emit(transform(it)) }
        }
    }
