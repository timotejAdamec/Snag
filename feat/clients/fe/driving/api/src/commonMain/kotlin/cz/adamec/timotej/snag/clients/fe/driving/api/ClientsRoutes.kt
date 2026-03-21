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

package cz.adamec.timotej.snag.clients.fe.driving.api

import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import kotlin.uuid.Uuid

interface ClientsRoute : SnagNavRoute {
    val onNewClientClick: () -> Unit
    val onClientClick: (clientId: Uuid) -> Unit
}

interface ClientsRouteFactory {
    fun create(
        onNewClientClick: () -> Unit,
        onClientClick: (clientId: Uuid) -> Unit,
    ): ClientsRoute
}

interface ClientCreationRoute : SnagNavRoute {
    val onCreated: (newClientId: Uuid) -> Unit
    val onDismiss: () -> Unit
}

interface ClientCreationRouteFactory {
    fun create(
        onCreated: (newClientId: Uuid) -> Unit,
        onDismiss: () -> Unit,
    ): ClientCreationRoute
}

interface ClientEditRoute : SnagNavRoute {
    val clientId: Uuid
    val onDismiss: () -> Unit
}

interface ClientEditRouteFactory {
    fun create(
        clientId: Uuid,
        onDismiss: () -> Unit,
    ): ClientEditRoute
}
