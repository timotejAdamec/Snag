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

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsNavRoute
import kotlin.uuid.Uuid

interface ClientsRoute : ProjectsNavRoute

interface ClientCreationRoute : ProjectsNavRoute {
    val onCreated: (newClientId: Uuid) -> Unit
}

interface ClientCreationRouteFactory {
    fun create(onCreated: (newClientId: Uuid) -> Unit): ClientCreationRoute
}

interface ClientEditRoute : ProjectsNavRoute {
    val clientId: Uuid
}

interface ClientEditRouteFactory {
    fun create(clientId: Uuid): ClientEditRoute
}
