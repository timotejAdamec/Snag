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

import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.app.impl.internal.ChangeUserRoleUseCaseImpl
import cz.adamec.timotej.snag.users.fe.app.impl.internal.GetUsersUseCaseImpl
import cz.adamec.timotej.snag.users.fe.app.impl.internal.sync.UserPullSyncHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersAppModule =
    module {
        factoryOf(::GetUsersUseCaseImpl) bind GetUsersUseCase::class
        factoryOf(::ChangeUserRoleUseCaseImpl) bind ChangeUserRoleUseCase::class
        factoryOf(::UserPullSyncHandler) bind PullSyncOperationHandler::class
    }
