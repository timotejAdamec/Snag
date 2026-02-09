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

package cz.adamec.timotej.snag.clients.fe.app.impl.di

import cz.adamec.timotej.snag.clients.fe.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.PullClientChangesUseCase
import cz.adamec.timotej.snag.clients.fe.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.DeleteClientUseCaseImpl
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.GetClientUseCaseImpl
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.GetClientsUseCaseImpl
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.PullClientChangesUseCaseImpl
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.SaveClientUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clientsAppModule =
    module {
        factoryOf(::GetClientsUseCaseImpl) bind GetClientsUseCase::class
        factoryOf(::GetClientUseCaseImpl) bind GetClientUseCase::class
        factoryOf(::SaveClientUseCaseImpl) bind SaveClientUseCase::class
        factoryOf(::DeleteClientUseCaseImpl) bind DeleteClientUseCase::class
        factoryOf(::PullClientChangesUseCaseImpl) bind PullClientChangesUseCase::class
    }
