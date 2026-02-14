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

package cz.adamec.timotej.snag.configuration.fe.impl.di

import cz.adamec.timotej.snag.configuration.fe.HttpClientConfiguration
import cz.adamec.timotej.snag.configuration.fe.impl.internal.ContentNegotiationConfiguration
import cz.adamec.timotej.snag.configuration.fe.impl.internal.LoggingConfiguration
import cz.adamec.timotej.snag.configuration.fe.impl.internal.ResponseValidationConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val configurationModule =
    module {
        singleOf(::LoggingConfiguration) bind HttpClientConfiguration::class
        singleOf(::ContentNegotiationConfiguration) bind HttpClientConfiguration::class
        singleOf(::ResponseValidationConfiguration) bind HttpClientConfiguration::class
    }
