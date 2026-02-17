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

package cz.adamec.timotej.snag.network.fe.app.impl.di

import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.app.impl.internal.InternetConnectionStatusListenerImpl
import cz.adamec.timotej.snag.network.fe.app.impl.internal.SnagNetworkHttpClientImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkAppModule =
    module {
        singleOf(::SnagNetworkHttpClientImpl) bind SnagNetworkHttpClient::class
        singleOf(::InternetConnectionStatusListenerImpl) bind InternetConnectionStatusListener::class
    }
