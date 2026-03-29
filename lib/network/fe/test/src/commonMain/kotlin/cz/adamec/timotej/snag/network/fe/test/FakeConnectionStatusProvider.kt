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

import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectionStatusProvider : ConnectionStatusProvider {
    private val connected = MutableStateFlow(true)

    override fun isConnectedFlow(): Flow<Boolean> = connected

    fun setConnected(value: Boolean) {
        connected.value = value
    }
}
