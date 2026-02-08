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

import cz.adamec.timotej.snag.clients.fe.driving.api.ClientCreationRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsBrowserHistoryFragmentBuilder
import cz.adamec.timotej.snag.clients.fe.driving.api.ClientsRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientCreationRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientEditRouteFactory
import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientsRoute
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserHistoryFragmentBuilder
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factory { WebClientsRoute } bind ClientsRoute::class
        factory { WebClientCreationRoute } bind ClientCreationRoute::class
        factory { WebClientEditRouteFactory() } bind ClientEditRouteFactory::class
        factoryOf(::ClientsBrowserHistoryFragmentBuilder) bind BrowserHistoryFragmentBuilder::class
    }
