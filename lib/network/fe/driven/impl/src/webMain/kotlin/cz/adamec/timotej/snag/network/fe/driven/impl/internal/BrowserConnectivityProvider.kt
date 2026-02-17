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

package cz.adamec.timotej.snag.network.fe.driven.impl.internal

internal interface BrowserConnectivityProvider {
    fun isOnline(): Boolean

    fun addConnectivityListeners(
        onOnline: () -> Unit,
        onOffline: () -> Unit,
    ): ListenerRegistration

    interface ListenerRegistration {
        fun unregister()
    }
}
