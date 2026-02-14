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

package cz.adamec.timotej.snag.impl.di

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.di.aggregate.be.backendModulesAggregate
import cz.adamec.timotej.snag.impl.internal.DevDataSeederConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val appModule =
    module {
        includes(backendModulesAggregate)
        singleOf(::DevDataSeederConfiguration) bind AppConfiguration::class
    }
