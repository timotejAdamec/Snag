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

package cz.adamec.timotej.snag.structures.be.app.impl.di

import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.GetStructuresUseCaseImpl
import cz.adamec.timotej.snag.structures.be.app.impl.internal.SaveStructureUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val structuresAppModule =
    module {
        factoryOf(::GetStructuresUseCaseImpl) bind GetStructuresUseCase::class
        factoryOf(::SaveStructureUseCaseImpl) bind SaveStructureUseCase::class
    }
