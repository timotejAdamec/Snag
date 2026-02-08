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

private fun jsIsNavigatorOnline(): Boolean =
    js("globalThis.navigator.onLine")

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

private fun jsRemoveConnectivityListeners(handlers: JsAny): Unit =
    js(
        """{
        globalThis.removeEventListener('online', handlers.online);
        globalThis.removeEventListener('offline', handlers.offline);
    }""",
    )

internal actual fun isNavigatorOnline(): Boolean = jsIsNavigatorOnline()

internal actual fun observeConnectivityChanges(
    onOnline: () -> Unit,
    onOffline: () -> Unit,
): ConnectivityEventRegistration {
    val handlers = jsAddConnectivityListeners(onOnline, onOffline)

    return object : ConnectivityEventRegistration {
        override fun unregister() {
            jsRemoveConnectivityListeners(handlers)
        }
    }
}
