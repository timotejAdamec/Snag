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

package cz.adamec.timotej.snag.network.fe.driven.impl.di

import cz.adamec.timotej.snag.network.fe.driven.impl.internal.ContentNegotiationConfiguration
import cz.adamec.timotej.snag.network.fe.driven.impl.internal.LoggingConfiguration
import cz.adamec.timotej.snag.network.fe.driven.impl.internal.ResponseValidationConfiguration
import cz.adamec.timotej.snag.network.fe.driven.impl.internal.RetryConfiguration
import cz.adamec.timotej.snag.network.fe.driven.impl.internal.ServerUrlFactoryImpl
import cz.adamec.timotej.snag.network.fe.ports.HttpClientConfiguration
import cz.adamec.timotej.snag.network.fe.ports.ServerUrlFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val networkDrivenModule =
    module {
        includes(serverUrlPlatformModule)
        includes(networkErrorClassifierPlatformModule)
        includes(connectionStatusPlatformModule)
        single<ServerUrlFactory> {
            ServerUrlFactoryImpl(
                localhostAddress = get<String>(named("localhostAddress")),
            )
        }
        singleOf(::LoggingConfiguration) bind HttpClientConfiguration::class
        singleOf(::ContentNegotiationConfiguration) bind HttpClientConfiguration::class
        singleOf(::ResponseValidationConfiguration) bind HttpClientConfiguration::class
        singleOf(::RetryConfiguration) bind HttpClientConfiguration::class
        single {
            HttpClient {
                getAll<HttpClientConfiguration>().forEach { configuration ->
                    with(configuration) { setup() }
                }
            }
        }
    }

internal expect val serverUrlPlatformModule: Module
internal expect val networkErrorClassifierPlatformModule: Module
internal expect val connectionStatusPlatformModule: Module
