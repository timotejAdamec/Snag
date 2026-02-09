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
data object NonWebClientsRoute : ClientsRoute

@Immutable
data class NonWebClientCreationRoute(
    override val onCreated: (Uuid) -> Unit,
) : ClientCreationRoute

class NonWebClientCreationRouteFactory : ClientCreationRouteFactory {
    override fun create(onCreated: (Uuid) -> Unit) = NonWebClientCreationRoute(onCreated)
}

@Serializable
@Immutable
data class NonWebClientEditRoute(
    override val clientId: Uuid,
) : ClientEditRoute

class NonWebClientEditRouteFactory : ClientEditRouteFactory {
    override fun create(clientId: Uuid): ClientEditRoute = NonWebClientEditRoute(clientId)
}
