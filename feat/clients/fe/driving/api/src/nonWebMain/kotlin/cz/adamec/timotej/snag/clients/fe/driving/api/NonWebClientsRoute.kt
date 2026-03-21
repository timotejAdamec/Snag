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
data class NonWebClientsRoute(
    override val onNewClientClick: () -> Unit,
    override val onClientClick: (Uuid) -> Unit,
) : ClientsRoute

class NonWebClientsRouteFactory : ClientsRouteFactory {
    override fun create(
        onNewClientClick: () -> Unit,
        onClientClick: (Uuid) -> Unit,
    ) = NonWebClientsRoute(
        onNewClientClick = onNewClientClick,
        onClientClick = onClientClick,
    )
}

@Immutable
data class NonWebClientCreationRoute(
    override val onCreated: (Uuid) -> Unit,
    override val onDismiss: () -> Unit,
) : ClientCreationRoute

class NonWebClientCreationRouteFactory : ClientCreationRouteFactory {
    override fun create(
        onCreated: (Uuid) -> Unit,
        onDismiss: () -> Unit,
    ) = NonWebClientCreationRoute(
        onCreated = onCreated,
        onDismiss = onDismiss,
    )
}

@Immutable
data class NonWebClientEditRoute(
    override val clientId: Uuid,
    override val onDismiss: () -> Unit,
) : ClientEditRoute

class NonWebClientEditRouteFactory : ClientEditRouteFactory {
    override fun create(
        clientId: Uuid,
        onDismiss: () -> Unit,
    ) = NonWebClientEditRoute(
        clientId = clientId,
        onDismiss = onDismiss,
    )
}
