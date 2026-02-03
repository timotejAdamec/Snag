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

package cz.adamec.timotej.snag.di

import cz.adamec.timotej.snag.di.aggregate.fe.frontendModulesAggregate
import cz.adamec.timotej.snag.logging.loggerModule
import cz.adamec.timotej.snag.ui.navigation.navigationModule
import org.koin.dsl.module

val appModule =
    module {
        includes(
            frontendModulesAggregate,
            loggerModule,
            navigationModule,
            mainModule,
        )
    }
