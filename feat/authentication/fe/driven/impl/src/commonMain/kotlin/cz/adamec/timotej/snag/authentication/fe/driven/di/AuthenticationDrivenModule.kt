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
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import org.koin.dsl.module

val authenticationDrivenModule =
    module {
        single<AuthTokenProvider> {
            if (CommonConfiguration.mockAuth) {
                MockAuthTokenProvider()
            } else {
                OidcAuthTokenProvider(
                    tokenStore = get(),
                    authFlowFactory = get(),
                )
            }
        }
    }
