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

import kotlinx.browser.window
import org.w3c.dom.events.Event

internal class JsBrowserConnectivityProvider : BrowserConnectivityProvider {
    override fun isOnline(): Boolean = window.navigator.onLine

    override fun addConnectivityListeners(
        onOnline: () -> Unit,
        onOffline: () -> Unit,
    ): BrowserConnectivityProvider.ListenerRegistration {
        val onlineHandler: (Event) -> Unit = { onOnline() }
        val offlineHandler: (Event) -> Unit = { onOffline() }

        window.addEventListener("online", onlineHandler)
        window.addEventListener("offline", offlineHandler)

        return object : BrowserConnectivityProvider.ListenerRegistration {
            override fun unregister() {
                window.removeEventListener("online", onlineHandler)
                window.removeEventListener("offline", offlineHandler)
            }
        }
    }
}
