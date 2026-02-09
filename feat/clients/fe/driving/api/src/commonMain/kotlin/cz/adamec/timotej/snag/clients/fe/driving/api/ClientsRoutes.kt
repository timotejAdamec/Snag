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

interface ClientsRoute : SnagNavRoute

interface ClientCreationRoute : SnagNavRoute

interface ClientEditRoute : SnagNavRoute {
    val clientId: Uuid
}

interface ClientEditRouteFactory {
    fun create(clientId: Uuid): ClientEditRoute
}
