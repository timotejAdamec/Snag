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

import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.internal.DefaultNetworkErrorClassifier
import cz.adamec.timotej.snag.network.fe.internal.KonnectionInternetConnectionStatusListener
import cz.adamec.timotej.snag.network.fe.internal.NetworkErrorClassifier
import dev.tmapps.konnection.Konnection
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val networkErrorClassifierPlatformModule =
    module {
        singleOf(::DefaultNetworkErrorClassifier) bind NetworkErrorClassifier::class
    }

internal actual val connectionStatusPlatformModule =
    module {
        single { Konnection.createInstance() }
        singleOf(::KonnectionInternetConnectionStatusListener) bind InternetConnectionStatusListener::class
    }
