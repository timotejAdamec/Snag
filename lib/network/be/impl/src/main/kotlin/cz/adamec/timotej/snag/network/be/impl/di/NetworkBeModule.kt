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

package cz.adamec.timotej.snag.network.be.impl.di

import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.network.be.impl.internal.CallLoggingConfiguration
import cz.adamec.timotej.snag.network.be.impl.internal.ContentNegotiationConfiguration
import cz.adamec.timotej.snag.network.be.impl.internal.CorsConfiguration
import cz.adamec.timotej.snag.network.be.impl.internal.RoutingConfiguration
import cz.adamec.timotej.snag.network.be.impl.internal.StatusPagesConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkBeModule =
    module {
        singleOf(::ContentNegotiationConfiguration) bind KtorServerConfiguration::class
        singleOf(::StatusPagesConfiguration) bind KtorServerConfiguration::class
        singleOf(::CorsConfiguration) bind KtorServerConfiguration::class
        singleOf(::CallLoggingConfiguration) bind KtorServerConfiguration::class
        singleOf(::RoutingConfiguration) bind KtorServerConfiguration::class
    }
