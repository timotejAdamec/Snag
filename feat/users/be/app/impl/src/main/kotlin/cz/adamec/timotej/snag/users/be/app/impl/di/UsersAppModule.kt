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

import cz.adamec.timotej.snag.users.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.users.be.app.api.SaveUserUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.AssignUserToProjectUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetProjectAssignmentsUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetUserUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.GetUsersUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.RemoveUserFromProjectUseCaseImpl
import cz.adamec.timotej.snag.users.be.app.impl.internal.SaveUserUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersAppModule =
    module {
        factoryOf(::GetUsersUseCaseImpl) bind GetUsersUseCase::class
        factoryOf(::GetUserUseCaseImpl) bind GetUserUseCase::class
        factoryOf(::SaveUserUseCaseImpl) bind SaveUserUseCase::class
        factoryOf(::GetProjectAssignmentsUseCaseImpl) bind GetProjectAssignmentsUseCase::class
        factoryOf(::AssignUserToProjectUseCaseImpl) bind AssignUserToProjectUseCase::class
        factoryOf(::RemoveUserFromProjectUseCaseImpl) bind RemoveUserFromProjectUseCase::class
    }
