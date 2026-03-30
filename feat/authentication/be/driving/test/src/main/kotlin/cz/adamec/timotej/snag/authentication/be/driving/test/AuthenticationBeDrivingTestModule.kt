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

package cz.adamec.timotej.snag.authentication.be.driving.test

import cz.adamec.timotej.snag.authentication.be.driving.impl.di.MOCK_AUTH_QUALIFIER
import org.koin.dsl.module

val authenticationBeDrivingTestModule =
    module {
        single(qualifier = MOCK_AUTH_QUALIFIER) { true }
    }
