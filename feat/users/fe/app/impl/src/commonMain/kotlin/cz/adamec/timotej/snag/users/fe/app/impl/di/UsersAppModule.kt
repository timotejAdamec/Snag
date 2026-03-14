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

package cz.adamec.timotej.snag.users.fe.app.impl.di

import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.app.api.PullUserChangesUseCase
import cz.adamec.timotej.snag.users.fe.app.impl.internal.GetUsersUseCaseImpl
import cz.adamec.timotej.snag.users.fe.app.impl.internal.PullUserChangesUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersAppModule =
    module {
        factoryOf(::GetUsersUseCaseImpl) bind GetUsersUseCase::class
        factoryOf(::PullUserChangesUseCaseImpl) bind PullUserChangesUseCase::class
    }
