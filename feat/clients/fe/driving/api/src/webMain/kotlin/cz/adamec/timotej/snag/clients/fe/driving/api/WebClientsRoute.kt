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

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Immutable
data object WebClientsRoute : ClientsRoute {
    const val URL_NAME = "clients"
}

@Immutable
data class WebClientCreationRoute(
    override val onCreated: (Uuid) -> Unit,
) : ClientCreationRoute {
    companion object {
        const val URL_NAME = "new-client"
    }
}

class WebClientCreationRouteFactory : ClientCreationRouteFactory {
    override fun create(onCreated: (Uuid) -> Unit) = WebClientCreationRoute(onCreated)
}

@Serializable
@Immutable
data class WebClientEditRoute(
    override val clientId: Uuid,
) : ClientEditRoute {
    companion object {
        const val URL_NAME = "edit-client"
    }
}

class WebClientEditRouteFactory : ClientEditRouteFactory {
    override fun create(clientId: Uuid): ClientEditRoute = WebClientEditRoute(clientId)
}
