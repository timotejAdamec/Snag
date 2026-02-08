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

@file:OptIn(ExperimentalWasmJsInterop::class)

package cz.adamec.timotej.snag.network.fe.internal

private fun jsIsOnline(): Boolean =
    js("globalThis.navigator.onLine")

@Suppress("unused")
private fun jsAddConnectivityListeners(
    onOnline: () -> Unit,
    onOffline: () -> Unit,
): JsAny =
    js(
        """(() => {
        const online = () => onOnline();
        const offline = () => onOffline();
        globalThis.addEventListener('online', online);
        globalThis.addEventListener('offline', offline);
        return { online, offline };
    })()""",
    )

@Suppress("unused")
private fun jsRemoveConnectivityListeners(handlers: JsAny): Unit =
    js(
        """{
        globalThis.removeEventListener('online', handlers.online);
        globalThis.removeEventListener('offline', handlers.offline);
    }""",
    )

internal class WasmBrowserConnectivityProvider : BrowserConnectivityProvider {
    override fun isOnline(): Boolean = jsIsOnline()

    override fun addConnectivityListeners(
        onOnline: () -> Unit,
        onOffline: () -> Unit,
    ): BrowserConnectivityProvider.ListenerRegistration {
        val handlers = jsAddConnectivityListeners(onOnline, onOffline)

        return object : BrowserConnectivityProvider.ListenerRegistration {
            override fun unregister() {
                jsRemoveConnectivityListeners(handlers)
            }
        }
    }
}
