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

package cz.adamec.timotej.snag.configuration.be.impl.di

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.configuration.be.impl.internal.CallLoggingConfiguration
import cz.adamec.timotej.snag.configuration.be.impl.internal.ContentNegotiationConfiguration
import cz.adamec.timotej.snag.configuration.be.impl.internal.CorsConfiguration
import cz.adamec.timotej.snag.configuration.be.impl.internal.RoutingConfiguration
import cz.adamec.timotej.snag.configuration.be.impl.internal.StatusPagesConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val configurationModule =
    module {
        singleOf(::ContentNegotiationConfiguration) bind AppConfiguration::class
        singleOf(::StatusPagesConfiguration) bind AppConfiguration::class
        singleOf(::CorsConfiguration) bind AppConfiguration::class
        singleOf(::CallLoggingConfiguration) bind AppConfiguration::class
        singleOf(::RoutingConfiguration) bind AppConfiguration::class
    }
