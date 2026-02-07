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
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.events.Event

internal class WebConnectionStatusListener : ConnectionStatusListener {
    override fun isConnectedFlow(): Flow<Boolean> =
        callbackFlow {
            trySend(window.navigator.onLine)

            val onlineHandler: (Event) -> Unit = { trySend(true) }
            val offlineHandler: (Event) -> Unit = { trySend(false) }

            window.addEventListener("online", onlineHandler)
            window.addEventListener("offline", offlineHandler)

            awaitClose {
                window.removeEventListener("online", onlineHandler)
                window.removeEventListener("offline", offlineHandler)
            }
        }.onEach { isConnected -> LH.logger.i { "Connection status changed: isConnected=$isConnected" } }
}
