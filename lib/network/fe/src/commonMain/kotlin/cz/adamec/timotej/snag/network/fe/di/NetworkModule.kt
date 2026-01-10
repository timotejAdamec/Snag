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

package cz.adamec.timotej.snag.network.fe.di

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.internal.HttpClientFactory
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single {
        SnagNetworkHttpClient(
            httpClient = HttpClientFactory.createHttpClient(),
        )
    }
}
