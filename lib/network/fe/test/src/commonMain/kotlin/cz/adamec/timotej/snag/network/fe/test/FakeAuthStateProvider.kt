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

package cz.adamec.timotej.snag.network.fe.test

import cz.adamec.timotej.snag.network.fe.ports.AuthStateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthStateProvider(
    initiallyReady: Boolean = true,
) : AuthStateProvider {
    private val _isReady = MutableStateFlow(initiallyReady)
    override val isReady: StateFlow<Boolean> = _isReady

    fun setReady(ready: Boolean) {
        _isReady.value = ready
    }
}
