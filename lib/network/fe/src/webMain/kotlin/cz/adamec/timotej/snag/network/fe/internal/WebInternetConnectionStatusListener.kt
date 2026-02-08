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

import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach

internal class WebInternetConnectionStatusListener(
    private val provider: BrowserConnectivityProvider,
) : InternetConnectionStatusListener {
    override fun isConnectedFlow(): Flow<Boolean> =
        callbackFlow {
            trySend(provider.isOnline())

            val registration =
                provider.addConnectivityListeners(
                    onOnline = { trySend(true) },
                    onOffline = { trySend(false) },
                )

            awaitClose { registration.unregister() }
        }.onEach { isConnected -> LH.logger.i { "Connection status: isConnected=$isConnected" } }
}
