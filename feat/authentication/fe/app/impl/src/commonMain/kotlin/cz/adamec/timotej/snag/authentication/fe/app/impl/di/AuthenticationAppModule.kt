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

package cz.adamec.timotej.snag.authentication.fe.app.impl.di

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAccessTokenUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthProviderIdUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.IsCurrentUserAuthenticatedFlowUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.LoginUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.LogoutUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.RefreshAccessTokenUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.RestoreSessionUseCase
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.GetAccessTokenUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.GetAuthProviderIdUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.IsCurrentUserAuthenticatedFlowUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LoginUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LogoutUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.RefreshAccessTokenUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.RestoreSessionUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authenticationAppModule =
    module {
        factoryOf(::GetAuthProviderIdUseCaseImpl) bind GetAuthProviderIdUseCase::class
        factoryOf(::GetAccessTokenUseCaseImpl) bind GetAccessTokenUseCase::class
        factoryOf(::RefreshAccessTokenUseCaseImpl) bind RefreshAccessTokenUseCase::class
        factoryOf(::LoginUseCaseImpl) bind LoginUseCase::class
        factoryOf(::LogoutUseCaseImpl) bind LogoutUseCase::class
        factoryOf(::RestoreSessionUseCaseImpl) bind RestoreSessionUseCase::class
        factoryOf(::IsCurrentUserAuthenticatedFlowUseCaseImpl) bind IsCurrentUserAuthenticatedFlowUseCase::class
    }
