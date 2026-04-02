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

package cz.adamec.timotej.snag.authentication.be.driving.impl.di

import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.AuthenticationStatusPageHandler
import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.CurrentUserConfiguration
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.configuration.be.AppStatusPageHandler
import cz.adamec.timotej.snag.configuration.be.BackendRunConfig
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val MOCK_AUTH_QUALIFIER = named("mockAuth")

val authenticationModule =
    module {
        single(qualifier = MOCK_AUTH_QUALIFIER) { BackendRunConfig.mockAuth }
        single<AppConfiguration> {
            CurrentUserConfiguration(
                getUserUseCase = get(),
                getOrCreateUserByAuthProviderIdUseCase = get(),
                mockAuth = get(qualifier = MOCK_AUTH_QUALIFIER),
            )
        }
        singleOf(::AuthenticationStatusPageHandler) bind AppStatusPageHandler::class
    }
