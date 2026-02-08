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

import cz.adamec.timotej.snag.network.fe.ConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.internal.NetworkErrorClassifier
import cz.adamec.timotej.snag.network.fe.internal.WebConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.internal.WebNetworkErrorClassifier
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val networkErrorClassifierPlatformModule =
    module {
        singleOf(::WebNetworkErrorClassifier) bind NetworkErrorClassifier::class
    }

internal actual val connectionStatusPlatformModule =
    module {
        includes(browserConnectivityProviderPlatformModule)
        singleOf(::WebConnectionStatusListener) bind ConnectionStatusListener::class
    }

internal expect val browserConnectivityProviderPlatformModule: Module
