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

package cz.adamec.timotej.snag.authentication.fe.wear.driven.di

import android.content.Context
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.wear.driven.internal.RealWearAuthFlow
import cz.adamec.timotej.snag.authentication.fe.wear.driven.internal.WearAuthFlow
import cz.adamec.timotej.snag.authentication.fe.wear.driven.internal.WearAuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val WEAR_AUTH_HTTP_CLIENT_QUALIFIER = named("wearAuthHttpClient")

val authenticationDrivenWearModule =
    module {
        single<HttpClient>(qualifier = WEAR_AUTH_HTTP_CLIENT_QUALIFIER) {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            explicitNulls = false
                        },
                    )
                }
            }
        }
        single<WearAuthFlow> {
            RealWearAuthFlow(
                context = get<Context>(),
                httpClient = get(qualifier = WEAR_AUTH_HTTP_CLIENT_QUALIFIER),
            )
        }
        single<AuthTokenProvider> {
            WearAuthTokenProvider(
                tokenStore = get(),
                authFlow = get(),
            )
        }
    }
