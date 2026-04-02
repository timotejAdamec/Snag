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

package cz.adamec.timotej.snag.authentication.fe.driven.di

import cz.adamec.timotej.snag.authentication.fe.driven.internal.MockAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.driven.internal.OidcAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.configuration.common.RunConfig
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val OIDC_REDIRECT_URI_QUALIFIER = named("oidcRedirectUri")

internal expect val platformModule: Module

val authenticationDrivenModule =
    module {
        includes(platformModule)
        single<AuthTokenProvider> {
            if (RunConfig.mockAuth) {
                MockAuthTokenProvider()
            } else {
                OidcAuthTokenProvider(
                    tokenStore = get(),
                    authFlowFactory = get(),
                    redirectUri = get(qualifier = OIDC_REDIRECT_URI_QUALIFIER),
                )
            }
        }
    }
