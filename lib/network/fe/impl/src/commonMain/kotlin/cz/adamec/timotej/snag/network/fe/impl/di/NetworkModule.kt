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

package cz.adamec.timotej.snag.network.fe.impl.di

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.impl.internal.AuthGatedSnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.impl.internal.SnagNetworkHttpClientImpl
import cz.adamec.timotej.snag.network.fe.impl.internal.ports.ContentNegotiationConfiguration
import cz.adamec.timotej.snag.network.fe.impl.internal.ports.LoggingConfiguration
import cz.adamec.timotej.snag.network.fe.impl.internal.ports.ResponseValidationConfiguration
import cz.adamec.timotej.snag.network.fe.impl.internal.ports.RetryConfiguration
import cz.adamec.timotej.snag.network.fe.ports.KtorClientConfiguration
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule =
    module {
        includes(networkErrorClassifierPlatformModule)
        includes(connectionStatusPlatformModule)
        singleOf(::LoggingConfiguration) bind KtorClientConfiguration::class
        singleOf(::ContentNegotiationConfiguration) bind KtorClientConfiguration::class
        singleOf(::ResponseValidationConfiguration) bind KtorClientConfiguration::class
        singleOf(::RetryConfiguration) bind KtorClientConfiguration::class
        single {
            HttpClient {
                getAll<KtorClientConfiguration>().forEach { configuration ->
                    with(configuration) { setup() }
                }
            }
        }
        single<SnagNetworkHttpClient> {
            AuthGatedSnagNetworkHttpClient(
                delegate =
                    SnagNetworkHttpClientImpl(
                        httpClient = get(),
                    ),
                authStateProvider = get(),
            )
        }
    }

internal expect val networkErrorClassifierPlatformModule: Module
internal expect val connectionStatusPlatformModule: Module
