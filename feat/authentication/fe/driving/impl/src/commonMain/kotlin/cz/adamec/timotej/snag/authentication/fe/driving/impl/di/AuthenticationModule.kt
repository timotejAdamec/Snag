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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.di

import cz.adamec.timotej.snag.authentication.fe.app.api.AuthenticatedUserProvider
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.AuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.AuthenticatedUserProviderImpl
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.CallCurrentUserConfiguration
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.MockAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.OAuthTokenProvider
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authenticationModule =
    module {
        single<AuthTokenProvider> {
            if (CommonConfiguration.mockAuth) {
                MockAuthTokenProvider()
            } else {
                OAuthTokenProvider()
            }
        }
        single<AuthenticatedUserProvider>(createdAtStart = true) {
            AuthenticatedUserProviderImpl(
                userId = MockAuthTokenProvider.MOCK_USER_UUID,
            )
        }
        singleOf(::CallCurrentUserConfiguration) bind HttpClientConfiguration::class
    }
