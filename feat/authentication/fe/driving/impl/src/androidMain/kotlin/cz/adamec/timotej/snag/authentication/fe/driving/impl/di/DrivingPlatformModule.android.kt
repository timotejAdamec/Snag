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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.di

import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.AndroidAuthComposeInitializer
import cz.adamec.timotej.snag.lib.design.fe.initializer.ComposeInitializer
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val drivingPlatformModule: Module =
    module {
        single { AndroidAuthComposeInitializer() } bind ComposeInitializer::class
    }
