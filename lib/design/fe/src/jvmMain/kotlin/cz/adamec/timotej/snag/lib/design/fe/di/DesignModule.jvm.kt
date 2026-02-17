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

package cz.adamec.timotej.snag.lib.design.fe.di

import cz.adamec.timotej.snag.lib.core.fe.Initializer
import cz.adamec.timotej.snag.lib.design.fe.initializers.JvmDesignInitializer
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule =
    module {
        factoryOf(::JvmDesignInitializer) bind Initializer::class
    }
