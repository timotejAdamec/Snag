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

package cz.adamec.timotej.snag.lib.design.fe.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Launches [collect] coroutines when at least one subscriber starts collecting
 * from this [MutableStateFlow], and cancels them after [stopTimeout] ms
 * of no subscribers. Returns this [MutableStateFlow] for chaining at the
 * property declaration site.
 */
fun <T> MutableStateFlow<T>.launchWhileSubscribed(
    scope: CoroutineScope,
    stopTimeout: Long = DEFAULT_NO_STATE_SUBSCRIBER_TIMEOUT,
    onSubscribe: (suspend () -> Unit)? = null,
    collect: () -> List<Job>,
): MutableStateFlow<T> {
    var continuousJobs = emptyList<Job>()
    var stopJob: Job? = null

    val subscriptionFlow =
        subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .onEach { hasSubscribers ->
                if (hasSubscribers) {
                    stopJob?.cancel()
                    onSubscribe?.let { scope.launch { it() } }
                    if (continuousJobs.none { it.isActive }) {
                        continuousJobs = collect()
                    }
                } else {
                    stopJob =
                        scope.launch {
                            delay(stopTimeout)
                            continuousJobs.forEach { it.cancel() }
                            continuousJobs = emptyList()
                        }
                }
            }
    subscriptionFlow.launchIn(scope)

    return this
}
