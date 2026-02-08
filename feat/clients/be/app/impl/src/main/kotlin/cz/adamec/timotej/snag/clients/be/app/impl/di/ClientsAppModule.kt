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

package cz.adamec.timotej.snag.clients.be.app.impl.di

import cz.adamec.timotej.snag.clients.be.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientsModifiedSinceUseCase
import cz.adamec.timotej.snag.clients.be.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.be.app.api.SaveClientUseCase
import cz.adamec.timotej.snag.clients.be.app.impl.internal.DeleteClientUseCaseImpl
import cz.adamec.timotej.snag.clients.be.app.impl.internal.GetClientUseCaseImpl
import cz.adamec.timotej.snag.clients.be.app.impl.internal.GetClientsModifiedSinceUseCaseImpl
import cz.adamec.timotej.snag.clients.be.app.impl.internal.GetClientsUseCaseImpl
import cz.adamec.timotej.snag.clients.be.app.impl.internal.SaveClientUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clientsAppModule =
    module {
        factoryOf(::GetClientsUseCaseImpl) bind GetClientsUseCase::class
        factoryOf(::GetClientUseCaseImpl) bind GetClientUseCase::class
        factoryOf(::SaveClientUseCaseImpl) bind SaveClientUseCase::class
        factoryOf(::DeleteClientUseCaseImpl) bind DeleteClientUseCase::class
        factoryOf(::GetClientsModifiedSinceUseCaseImpl) bind GetClientsModifiedSinceUseCase::class
    }
