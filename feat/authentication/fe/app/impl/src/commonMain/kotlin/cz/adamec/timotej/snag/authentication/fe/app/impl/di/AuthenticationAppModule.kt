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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthenticatedUserIdUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.LoginUseCase
import cz.adamec.timotej.snag.authentication.fe.app.api.LogoutUseCase
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.GetAuthenticatedUserIdUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LoginUseCaseImpl
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LogoutUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authenticationAppModule =
    module {
        factoryOf(::GetAuthenticatedUserIdUseCaseImpl) bind GetAuthenticatedUserIdUseCase::class
        factoryOf(::LoginUseCaseImpl) bind LoginUseCase::class
        factoryOf(::LogoutUseCaseImpl) bind LogoutUseCase::class
    }
