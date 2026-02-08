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

import kotlin.js.JsAny

@JsFun("() => globalThis.navigator.onLine")
private external fun jsIsNavigatorOnline(): Boolean

@JsFun(
    """
    (onOnline, onOffline) => {
        const onlineHandler = () => onOnline();
        const offlineHandler = () => onOffline();
        globalThis.addEventListener('online', onlineHandler);
        globalThis.addEventListener('offline', offlineHandler);
        return { onlineHandler, offlineHandler };
    }
    """,
)
private external fun jsAddConnectivityListeners(
    onOnline: () -> Unit,
    onOffline: () -> Unit,
): JsAny

@JsFun(
    """
    (handlers) => {
        globalThis.removeEventListener('online', handlers.onlineHandler);
        globalThis.removeEventListener('offline', handlers.offlineHandler);
    }
    """,
)
private external fun jsRemoveConnectivityListeners(handlers: JsAny)

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
