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

package cz.adamec.timotej.snag.network.fe.internal

import cz.adamec.timotej.snag.network.fe.ConnectionStatusListener
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

internal class KonnectionConnectionStatusListener(
    private val konnection: Konnection,
) : ConnectionStatusListener {
    override fun isConnectedFlow(): Flow<Boolean> =
        konnection.observeHasConnection()
            .onEach { isConnected -> LH.logger.i { "Connection status changed: isConnected=$isConnected" } }
}
