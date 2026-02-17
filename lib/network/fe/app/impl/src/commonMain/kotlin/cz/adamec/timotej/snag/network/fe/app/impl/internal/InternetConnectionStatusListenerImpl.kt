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

package cz.adamec.timotej.snag.network.fe.app.impl.internal

import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.ports.ConnectionStatusProvider
import kotlinx.coroutines.flow.Flow

internal class InternetConnectionStatusListenerImpl(
    private val connectionStatusProvider: ConnectionStatusProvider,
) : InternetConnectionStatusListener {
    override fun isConnectedFlow(): Flow<Boolean> = connectionStatusProvider.isConnectedFlow()
}
