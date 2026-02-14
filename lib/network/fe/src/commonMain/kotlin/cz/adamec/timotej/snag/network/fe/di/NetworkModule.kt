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

import cz.adamec.timotej.snag.configuration.fe.HttpClientConfiguration
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.internal.SnagNetworkHttpClientImpl
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule =
    module {
        includes(localHostPlatformModule)
        includes(networkErrorClassifierPlatformModule)
        includes(connectionStatusPlatformModule)
        single {
            HttpClient {
                getAll<HttpClientConfiguration>().forEach { configuration ->
                    with(configuration) { setup() }
                }
            }
        }
        single {
            SnagNetworkHttpClientImpl(
                httpClient = get(),
                localHostUrlFactory = get(),
            )
        } bind SnagNetworkHttpClient::class
    }

internal expect val localHostPlatformModule: Module
internal expect val networkErrorClassifierPlatformModule: Module
internal expect val connectionStatusPlatformModule: Module
