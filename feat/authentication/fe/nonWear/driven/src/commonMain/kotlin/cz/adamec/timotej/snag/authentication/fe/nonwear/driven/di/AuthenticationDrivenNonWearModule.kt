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

package cz.adamec.timotej.snag.authentication.fe.nonwear.driven.di

import cz.adamec.timotej.snag.authentication.fe.common.driven.di.OIDC_REDIRECT_URI_QUALIFIER
import cz.adamec.timotej.snag.authentication.fe.common.driven.internal.MockAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.common.driven.internal.OidcAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.configuration.common.RunConfig
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformNonWearAuthModule: Module

val authenticationDrivenNonWearModule =
    module {
        includes(platformNonWearAuthModule)
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
