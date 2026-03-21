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

package cz.adamec.timotej.snag.clients.fe.driving.impl.di

import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientCreationRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientEditRoute
import cz.adamec.timotej.snag.clients.fe.driving.api.WebClientsRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        clientsScreenNavigation<WebClientsRoute>()
        clientCreationScreenNavigation<WebClientCreationRoute>()
        clientEditScreenNavigation<WebClientEditRoute>()
    }
