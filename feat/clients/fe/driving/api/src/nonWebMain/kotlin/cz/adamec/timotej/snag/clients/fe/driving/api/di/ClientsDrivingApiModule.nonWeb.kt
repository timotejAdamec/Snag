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

package cz.adamec.timotej.snag.clients.fe.driving.api.di

import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.NonWebClientCreationRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.NonWebClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.NonWebClientsRoute
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { NonWebClientsRoute } bind ClientsRoute::class
        factory { NonWebClientCreationRouteFactory() } bind ClientCreationRouteFactory::class
        factory { NonWebClientEditRouteFactory() } bind ClientEditRouteFactory::class
    }
