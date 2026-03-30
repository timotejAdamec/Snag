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

import cz.adamec.timotej.snag.authentication.fe.driving.api.AuthenticationGateContent
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.AuthenticationGateContentImpl
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.AuthenticationInitializer
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.CallCurrentUserConfiguration
import cz.adamec.timotej.snag.authentication.fe.driving.impl.internal.vm.AuthenticationViewModel
import cz.adamec.timotej.snag.core.foundation.fe.Initializer
import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authenticationDrivingModule =
    module {
        includes(drivingPlatformModule)
        viewModelOf(::AuthenticationViewModel)
        singleOf(::CallCurrentUserConfiguration) bind HttpClientConfiguration::class
        singleOf(::AuthenticationInitializer) bind Initializer::class
        single<AuthenticationGateContent> { AuthenticationGateContentImpl() }
    }
