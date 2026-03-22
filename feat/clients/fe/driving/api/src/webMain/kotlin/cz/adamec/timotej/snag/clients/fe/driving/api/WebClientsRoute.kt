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
import kotlin.uuid.Uuid

@Immutable
data class WebClientsRoute(
    override val onNewClientClick: () -> Unit,
    override val onClientClick: (Uuid) -> Unit,
) : ClientsRoute {
    companion object {
        const val URL_NAME = "clients"
    }
}

class WebClientsRouteFactory : ClientsRouteFactory {
    override fun create(
        onNewClientClick: () -> Unit,
        onClientClick: (Uuid) -> Unit,
    ) = WebClientsRoute(
        onNewClientClick = onNewClientClick,
        onClientClick = onClientClick,
    )
}

@Immutable
data class WebClientCreationRoute(
    override val onCreated: (Uuid) -> Unit,
    override val onDismiss: () -> Unit,
) : ClientCreationRoute {
    companion object {
        const val URL_NAME = "new-client"
    }
}

class WebClientCreationRouteFactory : ClientCreationRouteFactory {
    override fun create(
        onCreated: (Uuid) -> Unit,
        onDismiss: () -> Unit,
    ) = WebClientCreationRoute(
        onCreated = onCreated,
        onDismiss = onDismiss,
    )
}

@Immutable
data class WebClientEditRoute(
    override val clientId: Uuid,
    override val onDismiss: () -> Unit,
) : ClientEditRoute {
    companion object {
        const val URL_NAME = "edit-client"
    }
}

class WebClientEditRouteFactory : ClientEditRouteFactory {
    override fun create(
        clientId: Uuid,
        onDismiss: () -> Unit,
    ) = WebClientEditRoute(
        clientId = clientId,
        onDismiss = onDismiss,
    )
}
