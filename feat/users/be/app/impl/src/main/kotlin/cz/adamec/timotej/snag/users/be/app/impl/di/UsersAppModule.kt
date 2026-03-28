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

package cz.adamec.timotej.snag.users.be.app.impl.di

import cz.adamec.timotej.snag.users.be.app.api.CanSetUserRoleUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByEntraIdUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersModifiedSinceUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.CanSetUserRoleUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetOrCreateUserByEntraIdUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetUserUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetUsersModifiedSinceUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetUsersUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.SaveUserUseCaseImpl
import cz.adamec.timotej.snag.users.business.CanSetUserRoleRule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersAppModule =
    module {
        factoryOf(::GetUsersUseCaseImpl) bind GetUsersUseCase::class
        factoryOf(::GetUserUseCaseImpl) bind GetUserUseCase::class
        factoryOf(::GetUsersModifiedSinceUseCaseImpl) bind GetUsersModifiedSinceUseCase::class
        factoryOf(::SaveUserUseCaseImpl) bind SaveUserUseCase::class
        factoryOf(::CanSetUserRoleRule)
        factoryOf(::GetOrCreateUserByEntraIdUseCaseImpl) bind GetOrCreateUserByEntraIdUseCase::class
        factoryOf(::CanSetUserRoleUseCaseImpl) bind CanSetUserRoleUseCase::class
    }
